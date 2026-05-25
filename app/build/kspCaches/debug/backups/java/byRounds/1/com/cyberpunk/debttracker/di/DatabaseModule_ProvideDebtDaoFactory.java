package com.cyberpunk.debttracker.di;

import com.cyberpunk.debttracker.data.db.DebtDao;
import com.cyberpunk.debttracker.data.db.DebtDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDebtDaoFactory implements Factory<DebtDao> {
  private final Provider<DebtDatabase> databaseProvider;

  public DatabaseModule_ProvideDebtDaoFactory(Provider<DebtDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DebtDao get() {
    return provideDebtDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideDebtDaoFactory create(
      Provider<DebtDatabase> databaseProvider) {
    return new DatabaseModule_ProvideDebtDaoFactory(databaseProvider);
  }

  public static DebtDao provideDebtDao(DebtDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDebtDao(database));
  }
}
