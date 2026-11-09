package com.quantumrift;

import com.quantumrift.world.RiftBiomes;
import terrablender.api.TerraBlenderApi;

public class QuantumRiftTerraBlender implements TerraBlenderApi {

    @Override
    public void onTerraBlenderInitialized() {
        RiftBiomes.register();
    }
}
