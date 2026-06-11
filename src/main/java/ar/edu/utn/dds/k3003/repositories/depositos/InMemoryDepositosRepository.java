package ar.edu.utn.dds.k3003.repositories.depositos;

import ar.edu.utn.dds.k3003.model.Deposito;
import lombok.val;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

//@Repository
//public class InMemoryDepositosRepository implements DepositosRepository {
//
//    private List<Deposito> depositos;
//    private AtomicLong idSecuencial = new AtomicLong(1);
//
//    public InMemoryDepositosRepository(){
//        this.depositos = new ArrayList<>();
//    }
//    @Override
//    public Deposito save(Deposito deposito) {
//        deposito.setId(String.valueOf(idSecuencial.getAndIncrement()));
//        this.depositos.add(deposito);
//        return deposito;
//    }
//
//    @Override
//    public Optional<Deposito> findById(String id) {
//        return this.depositos.stream().filter(d -> d.getId().equals(id)).findFirst();
//    }
//
//    @Override
//    public List<Deposito> findAll() {
//        return new ArrayList<>(this.depositos);
//    }
//
//    @Override
//    public Deposito deleteById(String id) {
//        val deposito = this.findById(id);
//        this.depositos.remove(deposito.get());
//        return deposito.get();
//    }
//}