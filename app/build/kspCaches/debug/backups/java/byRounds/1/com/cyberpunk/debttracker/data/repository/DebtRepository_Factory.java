package com.cyberpunk.debttracker.data.repository;

import com.cyberpunk.debttracker.data.db.DebtDao;
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
    "KotlinInternalInJava"
})
public final class DebtRepository_Factory implements Factory<DebtRepository> {
  private final Provider<DebtDao> debtDaoProvider;

  public DebtRepository_Factory(Provider<DebtDao> debtDaoProvider) {
    this.debtDaoProvider = debtDaoProvider;
  }

  @Override
  public DebtRepository get() {
    return newInstance(debtDaoProvider.get());
  }

  public static DebtRepository_Factory create(Provider<DebtDao> debtDaoProvider) {
    return new DebtRepository_Factory(debtDaoProvider);
  }

  public static DebtRepository newInstance(DebtDao debtDao) {
    return new DebtRepository(debtDao);
  }
}
