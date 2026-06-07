package com.example.demo.repository;

import com.example.demo.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    // JpaRepository를 상속받는 것만으로 
    // save(), findAll(), findById() 등의 메서드를 바로 사용할 수 있습니다.
}