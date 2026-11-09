package com.quantumrift.client;

import com.quantumrift.Constants;
import com.quantumrift.entity.VoidSovereign;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Hand-built model of the Void Sovereign (a tall armoured figure with horns, long arms, a cloak and four orbiting shards).
 * UV layout is shared with tools/gen_boss.py (128x128 texture): keep the texOffs values in sync.
 */
public class VoidSovereignModel extends HierarchicalModel<VoidSovereign> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "void_sovereign"), "main");
    private static final int SHARDS = 4;

    private final ModelPart root, head, rightArm, leftArm, rightLeg, leftLeg;
    private final ModelPart[] shards = new ModelPart[SHARDS];

    public VoidSovereignModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        for (int i = 0; i < SHARDS; i++) shards[i] = root.getChild("shard" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        p.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 30).addBox(-12.0F, -28.0F, -6.0F, 24.0F, 28.0F, 12.0F), PartPose.ZERO);
        PartDefinition head = p.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -14.0F, -7.0F, 14.0F, 14.0F, 14.0F), PartPose.offset(0.0F, -28.0F, 0.0F));
        head.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(74, 40).addBox(-1.5F, -11.0F, -1.5F, 3.0F, 11.0F, 3.0F), PartPose.offsetAndRotation(-5.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.45F));
        head.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(74, 40).mirror().addBox(-1.5F, -11.0F, -1.5F, 3.0F, 11.0F, 3.0F), PartPose.offsetAndRotation(5.0F, -12.0F, 0.0F, 0.0F, 0.0F, 0.45F));
        p.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(74, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 30.0F, 8.0F), PartPose.offset(-16.0F, -26.0F, 0.0F));
        p.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(74, 0).mirror().addBox(-4.0F, 0.0F, -4.0F, 8.0F, 30.0F, 8.0F), PartPose.offset(16.0F, -26.0F, 0.0F));
        p.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 72).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 24.0F, 8.0F), PartPose.offset(-6.0F, 0.0F, 0.0F));
        p.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 72).mirror().addBox(-4.0F, 0.0F, -4.0F, 8.0F, 24.0F, 8.0F), PartPose.offset(6.0F, 0.0F, 0.0F));
        p.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(32, 72).addBox(-11.0F, 0.0F, 0.0F, 22.0F, 34.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -28.0F, 6.5F, 0.08F, 0.0F, 0.0F));
        for (int i = 0; i < SHARDS; i++) {
            p.addOrReplaceChild("shard" + i, CubeListBuilder.create().texOffs(88, 40).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -50.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(VoidSovereign entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float swing = Mth.cos(limbSwing * 0.55F) * 1.1F * limbSwingAmount;
        rightLeg.xRot = swing;
        leftLeg.xRot = -swing;
        float idle = Mth.cos(ageInTicks * 0.06F) * 0.05F;
        if (entity.getCast() > 0) {      // arms raised while casting a pull or a slam
            float raise = -2.5F + Mth.sin(ageInTicks * 0.9F) * 0.12F;
            rightArm.xRot = raise; leftArm.xRot = raise;
            rightArm.zRot = 0.35F; leftArm.zRot = -0.35F;
        } else {
            rightArm.xRot = -swing * 0.5F + idle; leftArm.xRot = swing * 0.5F - idle;
            rightArm.zRot = 0.08F + idle; leftArm.zRot = -0.08F - idle;
        }
        float radius = 22.0F + 5.0F * (entity.getPhase() - 1);
        for (int i = 0; i < SHARDS; i++) {
            float a = ageInTicks * 0.06F * entity.getPhase() + i * Mth.HALF_PI;
            shards[i].x = Mth.cos(a) * radius;
            shards[i].z = Mth.sin(a) * radius;
            shards[i].y = -50.0F + Mth.sin(ageInTicks * 0.1F + i) * 3.0F;
            shards[i].yRot = -a;
            shards[i].xRot = ageInTicks * 0.08F + i;
        }
    }
}
