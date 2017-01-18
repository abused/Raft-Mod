package com.epiicthundercat.raft.client.renderer;

import com.epiicthundercat.raft.Reference;
import com.epiicthundercat.raft.client.model.ModelBarrel;
import com.epiicthundercat.raft.entity.FloatBarrel;

import net.minecraft.client.model.IMultipassModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFloatingBarrel extends Render<FloatBarrel>
{
    private static final ResourceLocation[] BARREL_TEXTURES = new ResourceLocation[] {new ResourceLocation(Reference.ID, "textures/entity/barrel/barrel_oak.png"), new ResourceLocation(Reference.ID, "textures/entity/barrel/barrel_spruce.png"), new ResourceLocation(Reference.ID, "textures/entity/barrel/barrel_birch.png"), new ResourceLocation(Reference.ID, "textures/entity/barrel/barrel_jungle.png"), new ResourceLocation(Reference.ID, "textures/entity/barrel/barrel_acacia.png"), new ResourceLocation(Reference.ID, "textures/entity/barrel/barrel_darkoak.png")};
    /** instance of ModelBarrel for rendering */
    protected ModelBase ModelBarrel = new ModelBarrel();

    public RenderFloatingBarrel(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(FloatBarrel entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.setupRotation(entity, entityYaw, partialTicks);
        this.bindEntityTexture(entity);

       

        this.ModelBarrel.render(entity, partialTicks, -0.1F, 0.0F, 0.0F, 0.0F, 0.0625F);


        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public void setupRotation(FloatBarrel p_188311_1_, float p_188311_2_, float p_188311_3_)
    {
    	GlStateManager.pushMatrix();
        GlStateManager.rotate(180.0F - p_188311_2_, 0.0F, 1.0F, 0.0F);
        float f = (float)p_188311_1_.getTimeSinceHit() - p_188311_3_;
        float f1 = p_188311_1_.getDamageTaken() - p_188311_3_;

        if (f1 < 0.0F)
        {
            f1 = 0.0F;
        }

        if (f > 0.0F)
        {
            GlStateManager.rotate(MathHelper.sin(f) * f * f1 / 10.0F * (float)p_188311_1_.getForwardDirection(), 1.0F, 0.0F, 0.0F);
        }

        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public void setupTranslation(double p_188309_1_, double p_188309_3_, double p_188309_5_)
    { 
    	
        GlStateManager.translate( p_188309_1_ + -0.4f, p_188309_3_ + 0.1f, p_188309_5_ + -0.4f);
      
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(FloatBarrel entity)
    {
        return BARREL_TEXTURES[entity.getBarrelType().ordinal()];
    }

    public boolean isMultipass()
    {
        return true;
    }

    public void renderMultipass(FloatBarrel p_188300_1_, double p_188300_2_, double p_188300_4_, double p_188300_6_, float p_188300_8_, float p_188300_9_)
    {
        GlStateManager.pushMatrix();
        this.setupTranslation(p_188300_2_, p_188300_4_, p_188300_6_);
        this.setupRotation(p_188300_1_, p_188300_8_, p_188300_9_);
        this.bindEntityTexture(p_188300_1_);
        ((IMultipassModel)this.ModelBarrel).renderMultipass(p_188300_1_, p_188300_9_, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}