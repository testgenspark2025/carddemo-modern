package com.carddemo;

import com.carddemo.account.CardDemoDataLoader;
import com.carddemo.auth.UserSeeder;
import com.carddemo.domain.account.AccountRepository;
import com.carddemo.domain.balance.TranCatBalanceEntity;
import com.carddemo.domain.balance.TranCatBalanceRepository;
import com.carddemo.domain.transaction.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end parity tests: each test asserts that the modern Java behavior matches
 * the COBOL behavior documented in COSGN00C, COACTVWC, and CBACT04C.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CarddemoIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired Flyway flyway;
    @Autowired AccountRepository accounts;
    @Autowired TranCatBalanceRepository tcatbals;
    @Autowired TransactionRepository transactions;
    @Autowired UserSeeder userSeeder;
    @Autowired CardDemoDataLoader dataLoader;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;

    static final ObjectMapper JSON = new ObjectMapper();
    MockMvc mvc;

    @BeforeAll
    void cleanDb() throws Exception {
        flyway.clean();
        flyway.migrate();
        // Re-apply Spring Batch metadata DDL (flyway.clean() dropped it).
        try (var conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn,
                    new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql"));
        }
        userSeeder.run();
        dataLoader.run();
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    }

    // ---------- COSGN00C parity ----------

    @Test @Order(1)
    @DisplayName("COSGN00C: valid admin login returns token, type=A, normalised user-id")
    void adminLogin_success() throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userId":"admin001","password":"PASSWORD"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("ADMIN001"))
                .andExpect(jsonPath("$.userType").value("A"))
                .andExpect(jsonPath("$.firstName").value("MARGARET"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        assertThat(JSON.readTree(r.getResponse().getContentAsString()).get("token").asText())
                .startsWith("eyJ");  // JWT header
    }

    @Test @Order(2)
    @DisplayName("COSGN00C: wrong password returns 401 with the legacy message")
    void wrongPassword_yieldsCobolMessage() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userId":"USER0001","password":"WRONGPWD"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }

    @Test @Order(3)
    @DisplayName("COSGN00C: unknown user returns 401 with the legacy message (RESP 13)")
    void unknownUser_yieldsCobolMessage() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userId":"NOPE0001","password":"PASSWORD"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found. Try again ..."));
    }

    // ---------- COACTVWC parity ----------

    @Test @Order(10)
    @DisplayName("COACTVWC: GET /api/accounts/{id} performs xref->acct->cust read chain")
    void accountView_happyPath() throws Exception {
        String token = login("ADMIN001", "PASSWORD");
        mvc.perform(get("/api/accounts/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(194.00))
                .andExpect(jsonPath("$.customer.firstName").value("Immanuel"))
                .andExpect(jsonPath("$.customer.lastName").value("Kessler"))
                // STRING formatting from COBOL paragraph 1200:
                .andExpect(jsonPath("$.customer.ssn").value("020-97-3888"));
    }

    @Test @Order(11)
    @DisplayName("COACTVWC: missing account -> 404 + 'Did not find this account in account card xref file'")
    void accountView_missing() throws Exception {
        String token = login("USER0001", "PASSWORD");
        mvc.perform(get("/api/accounts/9999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Did not find this account in account card xref file"));
    }

    @Test @Order(12)
    @DisplayName("COACTVWC: zero account id triggers the 2210-EDIT-ACCOUNT validation message")
    void accountView_zeroId() throws Exception {
        String token = login("USER0001", "PASSWORD");
        mvc.perform(get("/api/accounts/0").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Account number must be a non zero 11 digit number"));
    }

    @Test @Order(13)
    @DisplayName("COACTVWC: anonymous request to a protected endpoint is rejected")
    void accountView_noToken() throws Exception {
        mvc.perform(get("/api/accounts/1")).andExpect(status().isForbidden());
    }

    // ---------- CBACT04C parity ----------

    @Test @Order(20)
    @DisplayName("CBACT04C: batch math (bal * rate / 1200, HALF_UP) writes correct interest tx")
    void batch_interestCalc() throws Exception {
        // Arrange: seed three non-zero TCATBAL rows at acct 1,2,3 with known balances.
        // Their accounts already have group_id = A000000000 and that group has rate 15% for (01,1).
        List<TranCatBalanceEntity> all = tcatbals.findAllByOrderByAcctIdAscTranTypeCdAscTranCatCdAsc();
        all.get(0).setBalance(new BigDecimal("1000.00")); tcatbals.save(all.get(0));
        all.get(1).setBalance(new BigDecimal("2500.50")); tcatbals.save(all.get(1));
        all.get(2).setBalance(new BigDecimal("750.25"));  tcatbals.save(all.get(2));

        BigDecimal bal1Before = accounts.findById(1L).orElseThrow().getCurrentBalance();
        BigDecimal bal2Before = accounts.findById(2L).orElseThrow().getCurrentBalance();
        BigDecimal bal3Before = accounts.findById(3L).orElseThrow().getCurrentBalance();
        long txCountBefore = transactions.count();

        String adminToken = login("ADMIN001", "PASSWORD");
        mvc.perform(post("/api/batch/interest-calc?date=2025-05-24")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.writeCount").value(3));

        // Math parity:
        assertThat(transactions.count()).isEqualTo(txCountBefore + 3);
        assertThat(accounts.findById(1L).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo(bal1Before.add(new BigDecimal("12.50")));
        assertThat(accounts.findById(2L).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo(bal2Before.add(new BigDecimal("31.26")));
        assertThat(accounts.findById(3L).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo(bal3Before.add(new BigDecimal("9.38")));
        // Cycle counters reset (1050-UPDATE-ACCOUNT behaviour):
        assertThat(accounts.findById(1L).orElseThrow().getCurrCycCredit())
                .isEqualByComparingTo("0.00");
        assertThat(accounts.findById(1L).orElseThrow().getCurrCycDebit())
                .isEqualByComparingTo("0.00");
    }

    @Test @Order(21)
    @DisplayName("CBACT04C: non-admin user cannot trigger the batch (RACF / role gate)")
    void batch_requiresAdmin() throws Exception {
        String userToken = login("USER0001", "PASSWORD");
        mvc.perform(post("/api/batch/interest-calc?date=2025-05-24")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private String login(String userId, String password) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }
}
