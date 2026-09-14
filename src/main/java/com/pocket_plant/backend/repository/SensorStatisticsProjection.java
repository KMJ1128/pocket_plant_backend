package com.pocket_plant.backend.repository;

public interface SensorStatisticsProjection {
    Long getSampleCount();

    Double getTemperatureAverage();
    Float getTemperatureMinimum();
    Float getTemperatureMaximum();

    Double getHumidityAverage();
    Float getHumidityMinimum();
    Float getHumidityMaximum();

    Double getLightAverage();
    Float getLightMinimum();
    Float getLightMaximum();

    Double getSoilAverage();
    Float getSoilMinimum();
    Float getSoilMaximum();
}
