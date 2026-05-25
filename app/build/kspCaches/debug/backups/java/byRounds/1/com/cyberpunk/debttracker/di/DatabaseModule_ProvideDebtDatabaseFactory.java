package com.cyberpunk.debttracker.di;

import android.content.Context;
import com.cyberpunk.debttracker.data.db.DebtDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideDebtDatabaseFactory implements Factory<DebtDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideDebtDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DebtDatabase get() {
    return provideDebtDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideDebtDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideDebtDatabaseFactory(contextProvider);
  }

  public static DebtDatabase provideDebtDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDebtDatabase(context));
  }
}
