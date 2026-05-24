package com.carddemo.domain.disclosure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DisclosureGroupRepository
        extends JpaRepository<DisclosureGroupEntity, DisclosureGroupId> {
}
