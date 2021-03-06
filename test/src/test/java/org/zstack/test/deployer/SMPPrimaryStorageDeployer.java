package org.zstack.test.deployer;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.zstack.header.storage.primary.APIAddPrimaryStorageEvent;
import org.zstack.header.storage.primary.PrimaryStorageInventory;
import org.zstack.header.zone.ZoneInventory;
import org.zstack.storage.primary.nfs.APIAddNfsPrimaryStorageMsg;
import org.zstack.storage.primary.nfs.NfsPrimaryStorageConstant;
import org.zstack.storage.primary.smp.APIAddSharedMountPointPrimaryStorageMsg;
import org.zstack.storage.primary.smp.SMPPrimaryStorageSimulatorConfig;
import org.zstack.test.Api;
import org.zstack.test.ApiSender;
import org.zstack.test.ApiSenderException;
import org.zstack.test.deployer.schema.DeployerConfig;
import org.zstack.test.deployer.schema.SharedMountPointPrimaryStorageConfig;

import java.util.List;

@Configurable(preConstruction = true, autowire = Autowire.BY_TYPE)
public class SMPPrimaryStorageDeployer implements PrimaryStorageDeployer<SharedMountPointPrimaryStorageConfig> {
    @Autowired
    private SMPPrimaryStorageSimulatorConfig simulatorConfig;
    
    @Override
    public Class<SharedMountPointPrimaryStorageConfig> getSupportedDeployerClassType() {
        return SharedMountPointPrimaryStorageConfig.class;
    }

    @Override
    public void deploy(List<SharedMountPointPrimaryStorageConfig> primaryStorages, ZoneInventory zone, DeployerConfig config, Deployer deployer) throws ApiSenderException {
        Api api = deployer.getApi();
        for (SharedMountPointPrimaryStorageConfig nc : primaryStorages) {
            simulatorConfig.totalCapacity = deployer.parseSizeCapacity(nc.getTotalCapacity());
            simulatorConfig.availableCapcacity = deployer.parseSizeCapacity(nc.getAvailableCapacity());
            APIAddSharedMountPointPrimaryStorageMsg msg = new APIAddSharedMountPointPrimaryStorageMsg();
            msg.setName(nc.getName());
            msg.setUrl(nc.getUrl());
            msg.setDescription(nc.getDescription());
            msg.setType(NfsPrimaryStorageConstant.NFS_PRIMARY_STORAGE_TYPE);
            msg.setSession(api.getAdminSession());
            msg.setZoneUuid(zone.getUuid());
            ApiSender sender = api.getApiSender();
            APIAddPrimaryStorageEvent evt = sender.send(msg, APIAddPrimaryStorageEvent.class);
            PrimaryStorageInventory inv = evt.getInventory(); 
            deployer.primaryStorages.put(nc.getName(), inv);
        }
    }
}
