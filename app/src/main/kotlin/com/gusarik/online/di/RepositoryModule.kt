package com.gusarik.online.di

import com.gusarik.core.data.repository.AuthRepositoryImpl
import com.gusarik.core.data.repository.GameRepositoryImpl
import com.gusarik.core.data.repository.MatchRepositoryImpl
import com.gusarik.core.data.repository.UserRepositoryImpl
import com.gusarik.core.domain.repository.AuthRepository
import com.gusarik.core.domain.repository.GameRepository
import com.gusarik.core.domain.repository.MatchRepository
import com.gusarik.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository
}
