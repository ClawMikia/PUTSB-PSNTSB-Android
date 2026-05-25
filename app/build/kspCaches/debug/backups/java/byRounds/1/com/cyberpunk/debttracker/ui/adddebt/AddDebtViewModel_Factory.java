package com.cyberpunk.debttracker.ui.adddebt;

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
public final class AddDebtViewModel_Factory implements Factory<AddDebtViewModel> {
  private final Provider<DebtRepository> repositoryProvider;

  public AddDebtViewModel_Factory(Provider<DebtRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AddDebtViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static AddDebtViewModel_Factory create(Provider<DebtRepository> repositoryProvider) {
    return new AddDebtViewModel_Factory(repositoryProvider);
  }

  public static AddDebtViewModel newInstance(DebtRepository repository) {
    return new AddDebtViewModel(repository);
  }
}
