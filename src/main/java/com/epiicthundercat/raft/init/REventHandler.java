package com.epiicthundercat.raft.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.epiicthundercat.raft.Raft;
import com.epiicthundercat.raft.entity.FloatBarrel;
import com.epiicthundercat.raft.entity.PlankEntity;
import com.epiicthundercat.raft.entity.ScrapEntity;
import com.epiicthundercat.raft.entity.ThatchEntity;
import com.epiicthundercat.raft.init.barrel.BarrelLoot;
import com.epiicthundercat.raft.rafttileentitity.RenderTileBurner;
import com.epiicthundercat.raft.rafttileentitity.TileBurner;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class REventHandler {

	private static final int MOB_COUNT_DIV = (int) Math.pow(17.0D, 2.0D);
	private int ticks;
	/*
	 * EllPeck's Treasure Code!
	 */

	public static final List<BarrelLoot> scrap_loot = new ArrayList<BarrelLoot>();
	public static final List<BarrelLoot> barrel_loot = new ArrayList<BarrelLoot>();
	public static final List<BarrelLoot> thatch_loot = new ArrayList<BarrelLoot>();
	public static final List<BarrelLoot> plank_loot = new ArrayList<BarrelLoot>();

	public static void addBarrelLoot(ItemStack stack, int chance, int minAmount, int maxAmount) {
		barrel_loot.add(new BarrelLoot(stack, chance, minAmount, maxAmount));
	}

	public static void addThatchLoot(ItemStack stack, int chance, int minAmount, int maxAmount) {
		thatch_loot.add(new BarrelLoot(stack, chance, minAmount, maxAmount));
	}

	public static void addScrapLoot(ItemStack stack, int chance, int minAmount, int maxAmount) {
		scrap_loot.add(new BarrelLoot(stack, chance, minAmount, maxAmount));
	}

	public static void addPlankLoot(ItemStack stack, int chance, int minAmount, int maxAmount) {
		plank_loot.add(new BarrelLoot(stack, chance, minAmount, maxAmount));
	}

	@SubscribeEvent
	public void onTick(TickEvent.WorldTickEvent event) {
		World world = event.world;

		if (event.phase == TickEvent.Phase.END && world.provider.getDimension() == 0) {
			if (this.ticks == 20) {
				Random random = new Random();
				switch (random.nextInt(4)) {
				case 0:
					//System.out.println("SpawningB");
					trySpawnBarrel(world);
					break;
				case 1:
					//System.out.println("SpawningT");
					trySpawnThatch(world);
					break;
				case 2:
					//System.out.println("SpawningS");
					trySpawnScrap(world);
					break;
				case 3:
					//System.out.println("SpawningP");
					trySpawnPlank(world);
					break;
				}

				this.ticks = 0;
			}

			if (ticks % (2 * 60 * 20) == 0) {
				if (world.rand.nextBoolean())
					Raft.windX *= -1;

				if (world.rand.nextBoolean())
					Raft.windZ *= -1;

				Raft.network.sendToAll(new SendMovePack(Raft.windX, Raft.windZ));
			}

			this.ticks++;
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		Raft.network.sendTo(new SendMovePack(Raft.windX, Raft.windZ), (EntityPlayerMP) event.player);
	}

	private void trySpawnBarrel(World world) {
		Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();
		int chunks = 0;

		for (EntityPlayer entityplayer : world.playerEntities)
			if (!entityplayer.isSpectator()) {
				int playerX = MathHelper.floor(entityplayer.posX / 16.0D);
				int playerZ = MathHelper.floor(entityplayer.posZ / 16.0D);

				for (int x = -8; x <= 8; ++x)
					for (int z = -8; z <= 8; ++z) {
						boolean flag = x == -8 || x == 8 || z == -8 || z == 8;
						ChunkPos chunkcoordintpair = new ChunkPos(x + playerX, z + playerZ);

						if (!eligibleChunksForSpawning.contains(chunkcoordintpair)) {
							++chunks;

							if (!flag && world.getWorldBorder().contains(chunkcoordintpair))
								eligibleChunksForSpawning.add(chunkcoordintpair);

						}
					}
			}

		BlockPos spawnPoint = world.getSpawnPoint();
		int current = world.countEntities(FloatBarrel.class);
		int max = 5 * chunks;

		for (ChunkPos chunkcoordintpair : eligibleChunksForSpawning) {
			if (current > max)
				break;

			if (world.rand.nextFloat() < 0.1f) {
				BlockPos blockpos = getRandomChunkPosition(world, chunkcoordintpair.chunkXPos,
						chunkcoordintpair.chunkZPos);
				BlockPos waterBlock = null;
				int r = 4;

				for (int x = -r; x < r; x++)
					for (int y = -r; y < r; y++)
						for (int z = -r; z < r; z++) {
							BlockPos check = new BlockPos(blockpos.getX() + x, blockpos.getY() + y,
									blockpos.getZ() + z);
							Block block = world.getBlockState(check).getBlock();
							if (block == Blocks.WATER) {
								waterBlock = check;
								// break;
							}
						}

				if (waterBlock != null) {
					int x = waterBlock.getX();
					int y = waterBlock.getY();
					int z = waterBlock.getZ();

					if (spawnPoint.distanceSq((double) x, (double) y, (double) z) >= 2.0 * 2.0) {

						Biome biome = world.getBiome(waterBlock);
						int bg = 0;
						if (Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.OCEAN)
								|| Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.DEEP_OCEAN)) {
							FloatBarrel entity = new FloatBarrel(world);

							entity.setLocationAndAngles((double) x + 0.5, (double) y + 0.5, (double) z + 0.5, 0.0F,
									0.0F);

							current++;

							world.spawnEntity(entity);

						}
					}
				}
			}
		}
	}

	private void trySpawnThatch(World world) {
		Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();
		int chunks = 0;

		for (EntityPlayer entityplayer : world.playerEntities)
			if (!entityplayer.isSpectator()) {
				int playerX = MathHelper.floor(entityplayer.posX / 16.0D);
				int playerZ = MathHelper.floor(entityplayer.posZ / 16.0D);

				for (int x = -8; x <= 8; ++x)
					for (int z = -8; z <= 8; ++z) {
						boolean flag = x == -8 || x == 8 || z == -8 || z == 8;
						ChunkPos chunkcoordintpair = new ChunkPos(x + playerX, z + playerZ);

						if (!eligibleChunksForSpawning.contains(chunkcoordintpair)) {
							++chunks;

							if (!flag && world.getWorldBorder().contains(chunkcoordintpair))
								eligibleChunksForSpawning.add(chunkcoordintpair);
						}
					}
			}

		BlockPos spawnPoint = world.getSpawnPoint();
		int current = world.countEntities(ThatchEntity.class);
		int max = 10 * chunks;

		for (ChunkPos chunkcoordintpair : eligibleChunksForSpawning) {
			if (current > max)
				break;

			if (world.rand.nextFloat() < 0.1f) {
				BlockPos blockpos = getRandomChunkPosition(world, chunkcoordintpair.chunkXPos,
						chunkcoordintpair.chunkZPos);
				BlockPos waterBlock = null;
				int r = 4;

				for (int x = -r; x < r; x++)
					for (int y = -r; y < r; y++)
						for (int z = -r; z < r; z++) {
							BlockPos check = new BlockPos(blockpos.getX() + x, blockpos.getY() + y,
									blockpos.getZ() + z);
							Block block = world.getBlockState(check).getBlock();
							if (block == Blocks.WATER) {
								waterBlock = check;
								break;
							}
						}

				if (waterBlock != null) {
					int x = waterBlock.getX();
					int y = waterBlock.getY();
					int z = waterBlock.getZ();

					if (spawnPoint.distanceSq((double) x, (double) y, (double) z) >= 2.0 * 2.0) {
						Biome biome = world.getBiome(waterBlock);
						int bg = 0;
						if (Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.OCEAN)
								|| Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.DEEP_OCEAN)) {
							ThatchEntity entity = new ThatchEntity(world);
							entity.setLocationAndAngles((double) x + 0.5, (double) y + 0.5, (double) z + 0.5, 0.0F,
									0.0F);

							current++;

							world.spawnEntity(entity);

						}
					}
				}
			}
		}
	}

	private void trySpawnPlank(World world) {
		Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();
		int chunks = 0;

		for (EntityPlayer entityplayer : world.playerEntities)
			if (!entityplayer.isSpectator()) {
				int playerX = MathHelper.floor(entityplayer.posX / 16.0D);
				int playerZ = MathHelper.floor(entityplayer.posZ / 16.0D);

				for (int x = -8; x <= 8; ++x)
					for (int z = -8; z <= 8; ++z) {
						boolean flag = x == -8 || x == 8 || z == -8 || z == 8;
						ChunkPos chunkcoordintpair = new ChunkPos(x + playerX, z + playerZ);

						if (!eligibleChunksForSpawning.contains(chunkcoordintpair)) {
							++chunks;

							if (!flag && world.getWorldBorder().contains(chunkcoordintpair))
								eligibleChunksForSpawning.add(chunkcoordintpair);
						}
					}
			}

		BlockPos spawnPoint = world.getSpawnPoint();
		int current = world.countEntities(PlankEntity.class);
		int max = 10 * chunks;

		for (ChunkPos chunkcoordintpair : eligibleChunksForSpawning) {
			if (current > max)
				break;

			if (world.rand.nextFloat() < 0.1f) {
				BlockPos blockpos = getRandomChunkPosition(world, chunkcoordintpair.chunkXPos,
						chunkcoordintpair.chunkZPos);
				BlockPos waterBlock = null;
				int r = 4;

				for (int x = -r; x < r; x++)
					for (int y = -r; y < r; y++)
						for (int z = -r; z < r; z++) {
							BlockPos check = new BlockPos(blockpos.getX() + x, blockpos.getY() + y,
									blockpos.getZ() + z);
							Block block = world.getBlockState(check).getBlock();
							if (block == Blocks.WATER) {
								waterBlock = check;
								break;
							}
						}

				if (waterBlock != null) {
					int x = waterBlock.getX();
					int y = waterBlock.getY();
					int z = waterBlock.getZ();

					if (spawnPoint.distanceSq((double) x, (double) y, (double) z) >= 2.0 * 2.0) {
						Biome biome = world.getBiome(waterBlock);
						int bg = 0;
						if (Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.OCEAN)
								|| Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.DEEP_OCEAN)) {
							PlankEntity entity = new PlankEntity(world);
							entity.setLocationAndAngles((double) x + 0.5, (double) y + 0.5, (double) z + 0.5, 0.0F,
									0.0F);

							current++;

							world.spawnEntity(entity);

						}
					}
				}
			}
		}
	}

	private void trySpawnScrap(World world) {
		Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();
		int chunks = 0;

		for (EntityPlayer entityplayer : world.playerEntities)
			if (!entityplayer.isSpectator()) {
				int playerX = MathHelper.floor(entityplayer.posX / 16.0D);
				int playerZ = MathHelper.floor(entityplayer.posZ / 16.0D);

				for (int x = -8; x <= 8; ++x)
					for (int z = -8; z <= 8; ++z) {
						boolean flag = x == -8 || x == 8 || z == -8 || z == 8;
						ChunkPos chunkcoordintpair = new ChunkPos(x + playerX, z + playerZ);

						if (!eligibleChunksForSpawning.contains(chunkcoordintpair)) {
							++chunks;

							if (!flag && world.getWorldBorder().contains(chunkcoordintpair))
								eligibleChunksForSpawning.add(chunkcoordintpair);
						}
					}
			}

		BlockPos spawnPoint = world.getSpawnPoint();
		int current = world.countEntities(ScrapEntity.class);
		int max = 10 * chunks;

		for (ChunkPos chunkcoordintpair : eligibleChunksForSpawning) {
			if (current > max)
				break;

			if (world.rand.nextFloat() < 0.1f) {
				BlockPos blockpos = getRandomChunkPosition(world, chunkcoordintpair.chunkXPos,
						chunkcoordintpair.chunkZPos);
				BlockPos waterBlock = null;
				int r = 4;

				for (int x = -r; x < r; x++)
					for (int y = -r; y < r; y++)
						for (int z = -r; z < r; z++) {
							BlockPos check = new BlockPos(blockpos.getX() + x, blockpos.getY() + y,
									blockpos.getZ() + z);
							Block block = world.getBlockState(check).getBlock();
							if (block == Blocks.WATER) {
								waterBlock = check;
								break;
							}
						}

				if (waterBlock != null) {
					int x = waterBlock.getX();
					int y = waterBlock.getY();
					int z = waterBlock.getZ();

					if (spawnPoint.distanceSq((double) x, (double) y, (double) z) >= 2.0 * 2.0) {
						Biome biome = world.getBiome(waterBlock);
						int bg = 0;
						if (Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.OCEAN)
								|| Biome.getIdForBiome(biome) == Biome.getIdForBiome(Biomes.DEEP_OCEAN)) {
							ScrapEntity entity = new ScrapEntity(world);
							entity.setLocationAndAngles((double) x + 0.5, (double) y + 0.5, (double) z + 0.5, 0.0F,
									0.0F);

							current++;

							world.spawnEntity(entity);

						}
					}
				}
			}
		}
	}

	private static BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
		Chunk chunk = worldIn.getChunkFromChunkCoords(x, z);
		int i = x * 16 + worldIn.rand.nextInt(16);
		int j = z * 16 + worldIn.rand.nextInt(16);
		int k = chunk.getHeight(new BlockPos(i, 0, j)) + 1;

		return new BlockPos(i, k, j);
	}

	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent event) {
		EntityPlayer player = event.player;
		EntityItem entity = event.pickedUp;
		ItemStack itemstack = entity.getEntityItem();

		if (itemstack != null && itemstack.getItem() == Item.getItemFromBlock(RBlocks.palm_log)) {
			player.addStat(AchievementList.MINE_WOOD);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileBurner.class, new RenderTileBurner());
	}
}
