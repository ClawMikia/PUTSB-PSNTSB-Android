package com.cyberpunk.debttracker.data.repository;

import com.cyberpunk.debttracker.data.db.DebtDao;
import com.cyberpunk.debttracker.game.GameEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DebtRepository_Factory implements Factory<DebtRepository> {
  private final Provider<DebtDao> debtDaoProvider;

  private final Provider<GameEngine> gameProvider;

  public DebtRepository_Factory(Provider<DebtDao> debtDaoProvider,
      Provider<GameEngine> gameProvider) {
    this.debtDaoProvider = debtDaoProvider;
    this.gameProvider = gameProvider;
  }

  @Override
  public DebtRepository get() {
    return newInstance(debtDaoProvider.get(), gameProvider.get());
  }

  public static DebtRepository_Factory create(Provider<DebtDao> debtDaoProvider,
      Provider<GameEngine> gameProvider) {
    return new DebtRepository_Factory(debtDaoProvider, gameProvider);
  }

  public static DebtRepository newInstance(DebtDao debtDao, GameEngine game) {
    return new DebtRepository(debtDao, game);
  }
}
