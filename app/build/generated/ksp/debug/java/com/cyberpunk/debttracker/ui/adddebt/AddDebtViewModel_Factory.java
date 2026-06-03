package com.cyberpunk.debttracker.ui.adddebt;

import android.app.Application;
import com.cyberpunk.debttracker.data.repository.DebtRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AddDebtViewModel_Factory implements Factory<AddDebtViewModel> {
  private final Provider<DebtRepository> repositoryProvider;

  private final Provider<Application> applicationProvider;

  public AddDebtViewModel_Factory(Provider<DebtRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    this.repositoryProvider = repositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public AddDebtViewModel get() {
    return newInstance(repositoryProvider.get(), applicationProvider.get());
  }

  public static AddDebtViewModel_Factory create(Provider<DebtRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    return new AddDebtViewModel_Factory(repositoryProvider, applicationProvider);
  }

  public static AddDebtViewModel newInstance(DebtRepository repository, Application application) {
    return new AddDebtViewModel(repository, application);
  }
}
