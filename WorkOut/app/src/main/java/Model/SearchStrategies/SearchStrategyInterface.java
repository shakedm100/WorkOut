package Model.SearchStrategies;

import java.util.ArrayList;

import Model.Business;

public interface SearchStrategyInterface<T>
{
    abstract ArrayList<Business> filter(T... args);
}
