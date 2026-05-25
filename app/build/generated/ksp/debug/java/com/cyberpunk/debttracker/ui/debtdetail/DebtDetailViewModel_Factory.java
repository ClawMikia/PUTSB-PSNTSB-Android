package com.cyberpunk.debttracker.ui.debtdetail;

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
    "KotlinInternalInJava"
})
public final class DebtDetailViewModel_Factory implements Factory<DebtDetailViewModel> {
  private final Provider<DebtRepository> repositoryProvider;

  public DebtDetailViewModel_Factory(Provider<DebtRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DebtDetailViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static DebtDetailViewModel_Factory create(Provider<DebtRepository> repositoryProvider) {
    return new DebtDetailViewModel_Factory(repositoryProvider);
  }

  public static DebtDetailViewModel newInstance(DebtRepository repository) {
    return new DebtDetailViewModel(repository);
  }
}
