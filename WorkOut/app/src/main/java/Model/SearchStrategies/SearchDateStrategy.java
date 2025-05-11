package Model.SearchStrategies;

import java.time.LocalDateTime;
import java.util.ArrayList;

import Model.Business;

public class SearchDateStrategy implements SearchStrategyInterface<LocalDateTime>
{
    @Override
    public ArrayList<Business> filter(LocalDateTime... args)
    {
        return null;
    }
}
