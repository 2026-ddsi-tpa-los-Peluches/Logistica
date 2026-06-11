package ar.edu.utn.dds.k3003.repositories.depositos;

import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;



public interface DepositosRepository extends JpaRepository<Deposito, Integer> {
}
//public interface DepositosRepository {
//
//    Deposito save(Deposito deposito);
//
//    Optional<Deposito> findById(String id);
//
//    List<Deposito> findAll();
//
//    Deposito deleteById(String id);
//}
