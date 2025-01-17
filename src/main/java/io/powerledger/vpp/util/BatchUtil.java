package io.powerledger.vpp.util;

import io.powerledger.vpp.dto.BatteryDto;

import java.util.ArrayList;
import java.util.List;

public class BatchUtil {

    public static List<List<BatteryDto>> splitBatteriesIntoBatches(List<BatteryDto> batteries, int batchSize) {
        List<List<BatteryDto>> batches = new ArrayList<>();
        for (int i = 0; i < batteries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, batteries.size());
            batches.add(batteries.subList(i, end));
        }
        return batches;
    }
}
