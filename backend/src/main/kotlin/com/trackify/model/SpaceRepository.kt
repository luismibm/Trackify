package com.trackify.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SpaceRepository : JpaRepository<Space, UUID> 