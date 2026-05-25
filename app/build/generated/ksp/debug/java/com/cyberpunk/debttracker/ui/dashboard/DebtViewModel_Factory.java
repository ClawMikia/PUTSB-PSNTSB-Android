package com.cyberpunk.debttracker.ui.dashboard;

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
public final class DebtViewModel_Factory implements Factory<DebtViewModel> {
  private final Provider<DebtRepository> repositoryProvider;

  public DebtViewModel_Factory(Provider<DebtRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DebtViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static DebtViewModel_Factory create(Provider<DebtRepository> repositoryProvider) {
    return new DebtViewModel_Factory(repositoryProvider);
  }

  public static DebtViewModel newInstance(DebtRepository repository) {
    return new DebtViewModel(repository);
  }
}
