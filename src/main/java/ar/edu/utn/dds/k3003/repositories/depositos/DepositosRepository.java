package ar.edu.utn.dds.k3003.repositories.depositos;

import ar.edu.utn.dds.k3003.model.Deposito;

import java.util.List;
import java.util.Optional;

public interface DepositosRepository {

    Deposito save(Deposito deposito);

    Optional<Deposito> findById(String id);

    List<Deposito> findAll();

    Deposito deleteById(String id);
}
