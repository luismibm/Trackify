package com.luismibm.android.models

data class UpdateSpaceRequest(
    val spaceId: String?
)

data class SpaceRequest(
    val name: String,
    val accessCode: String
)

data class Space(
    val id: String,
    val name: String
)

data class VerifySpaceAccessRequest(
    val accessCode: String
)