package com.aditya.learningspringboot._8TransactionManagement._2Programmatic;

import org.springframework.context.annotation.Scope;

@Scope(scopeName = PROTOT)
public class Programattic2MoreConcise {
}

//@Service
//public class UserService {
//
//    private final TransactionTemplate transactionTemplate;
//    private final UserRepository userRepository;
//    private final ExternalApiClient externalApi;
//
//    public UserService(PlatformTransactionManager txManager,
//                       UserRepository userRepository,
//                       ExternalApiClient externalApi) {
//        this.transactionTemplate = new TransactionTemplate(txManager);
//        this.userRepository = userRepository;
//        this.externalApi = externalApi;
//    }
//
//    public void updateUser(Long userId, SomePayload payload) {
//        // step 1: run DB work in its own transaction
//        transactionTemplate.execute(status -> {
//            User user = userRepository.findById(userId).orElseThrow();
//            user.applySomeChange(payload);
//            userRepository.save(user);
//            return null;
//        });
//
//        // external call outside transaction
//        ExternalResponse resp = externalApi.call(payload);
//
//        // step 2: separate transaction for final DB update
//        transactionTemplate.execute(status -> {
//            User user = userRepository.findById(userId).orElseThrow();
//            user.applyResult(resp);
//            userRepository.save(user);
//            return null;
//        });
//    }
//
