package com.aditya.learningspringboot._5TransactionManagement._2Programmatic;

public class Pragramattic1 {

}

//@Service
//public class UserService {
//
//    private final PlatformTransactionManager txManager;
//    private final UserRepository userRepository;
//    private final ExternalApiClient externalApi;
//
//    public UserService(PlatformTransactionManager txManager,
//                       UserRepository userRepository,
//                       ExternalApiClient externalApi) {
//        this.txManager = txManager;
//        this.userRepository = userRepository;
//        this.externalApi = externalApi;
//    }
//
//    public void updateUser(Long userId, SomePayload payload) {
//        // FIRST TRANSACTION: update DB and commit immediately
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("updateUser-step1");
//        TransactionStatus status1 = txManager.getTransaction(def);
//        try {
//            User user = userRepository.findById(userId).orElseThrow();
//            user.applySomeChange(payload);
//            userRepository.save(user);
//            txManager.commit(status1);   // connection returned to pool here
//        } catch (RuntimeException ex) {
//            txManager.rollback(status1);
//            throw ex;
//        }
//
//        // EXTERNAL CALL outside any transaction (no DB connection held)
//        ExternalResponse resp = externalApi.call(payload);
//
//        // SECOND TRANSACTION: update DB again and commit
//        TransactionStatus status2 = txManager.getTransaction(def);
//        try {
//            User user = userRepository.findById(userId).orElseThrow();
//            user.applyResult(resp);
//            userRepository.save(user);
//            txManager.commit(status2);
//        } catch (RuntimeException ex) {
//            txManager.rollback(status2);
//            // decide compensation or compensating action if necessary
//            throw ex;
//        }
//    }
//}
