package com.se1873.js.springboot.project.component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntityRelationshipHandler {
  private final EntityManager entityManager;
  @Transactional(rollbackFor = Exception.class)
  public <T, P> void setRelationships(Collection<T> newEntities, P parent,
                                      BiConsumer<T, P> relationshipSetter) {
    Optional.ofNullable(newEntities)
      .ifPresent(items -> {
        items.forEach(entity -> {
          try {
            if (entityManager.contains(entity)) {
              entityManager.refresh(entity); //Refresh lại entity nếu chưa được quản l
            }

            P managedParent = entityManager.contains(parent) ? //Merge với parent nếu bị DETACHED
              parent : entityManager.merge(parent);

            T managedEntity = entityManager.contains(entity) ? //Merge nếu đã tồn tại và persist nếu tạo mới
              entity : entityManager.merge(entity);

            relationshipSetter.accept(managedEntity, managedParent);

            entityManager.flush(); //FLush NGAY LẬP TỨC để kiểm tra xung đột
          } catch (OptimisticLockException e) {
            log.error("Concurrent modification detected for entity: {}", entity);
            throw new ConcurrentModificationException(
              "Entity was modified by another transaction. Please refresh and try again.", e);
          } catch (Exception e) {
            log.error("Error setting relationship for entity: {} with parent: {}",
              entity, parent, e);
            throw new RuntimeException("Failed to set entity relationship", e);
          }
        });
      });
  }
}
