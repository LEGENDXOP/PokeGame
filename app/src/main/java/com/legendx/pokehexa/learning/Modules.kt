package com.legendx.pokehexa.learning

import EmailValidator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val appModule = module{
    single<PatternValidator>{ EmailValidator() }
    single { UserValidator(get())}
    singleOf(::MyViewModel)
}