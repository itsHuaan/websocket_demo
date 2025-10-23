package com.example.websocket_demo.util;

import org.apache.batik.apps.svgbrowser.Application;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.Context;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Component
public class Mapper implements ApplicationListener<ContextRefreshedEvent> {
    private final Map<Key, BiConsumer<Object, Object>> customizers = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.initialized = true;
    }

    public <T> T map(Object src, Class<T> targetType) {
        if (src == null) return null;
        try {
            T target = targetType.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(src, target);
            applyCustomizer(src.getClass(), targetType, src, target);
            return target;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public <S, T> T update(S src, T target, boolean ignoreNulls) {
        if (src == null || target == null) return target;
        if (ignoreNulls) BeanUtils.copyProperties(src, target, nullProps(src));
        else BeanUtils.copyProperties(src, target);
        applyCustomizer(src.getClass(), target.getClass(), src, target);
        return target;
    }

    public <E, D> D toDto(E entity, Class<D> dtoType) {
        return map(entity, dtoType);
    }

    public <D, E> E toEntity(D dto, Class<E> entityType) {
        return map(dto, entityType);
    }

    @SuppressWarnings("unchecked")
    public <S, T> void registerCustomizer(Class<S> s, Class<T> t, BiConsumer<S, T> fn) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot register customizers after the application has started.");
        }
        customizers.put(new Key(s, t), (BiConsumer<Object, Object>) fn);
    }

    private <S, T> void applyCustomizer(Class<S> s, Class<T> t, Object src, Object tar) {
        BiConsumer<Object, Object> c = customizers.get(new Key(s, t));
        if (c != null) c.accept(src, tar);
    }


    private String[] nullProps(Object source) {
        BeanWrapper bw = new BeanWrapperImpl(source);
        Set<String> empty = new HashSet<>();
        for (PropertyDescriptor pd : bw.getPropertyDescriptors()) {
            if (bw.getPropertyValue(pd.getName()) == null) empty.add(pd.getName());
        }
        return empty.toArray(new String[0]);
    }

    private record Key(Class<?> s, Class<?> t) {
    }
}
