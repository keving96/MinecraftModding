package com.mymod.moc;

import java.util.Map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod(MineOreCluster.MOD_ID)
public class MineOreCluster {

    public static final String MOD_ID = "moc";

    private Player player;
    private LevelAccessor world;
    private ItemStack itemStack;
    private BlockState blockState;
    private Item item;
    public static boolean mocDisabled = false;
    private int fortune;
    private int silktouch;

    public MineOreCluster() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if (!mocDisabled) {
            BlockPos blockPos = event.getPos();
            this.blockState = event.getState();
            Block block = this.blockState.getBlock();
            this.player = event.getPlayer();
            this.itemStack = this.player.getMainHandItem();
            this.item = this.itemStack.getItem();
            this.world = event.getLevel();
            assert Minecraft.getInstance().player != null;
            boolean isCreative = Minecraft.getInstance().player.isCreative(); //playerController.isInCreativeMode();

            // Set values for variables fortune and silktouch
            handleEnchantment();

            // TODO: have a look at canAttackBlock(BlockState, Level, BlockPos, Player)
            if (isOreBlock(block)
                    && this.item instanceof net.minecraft.world.item.PickaxeItem
                    && this.item.canAttackBlock(this.blockState, (Level) this.world, blockPos, this.player)
                    && !isCreative) {
                if (hasAnyNeighbourOfSameType(blockPos, block)) {
                    for (Direction direction : Direction.values()) {
                        mineOres(direction, block, blockPos);
                    }
                    // disabling this would calculate x+1 destroyed block instead of x number of destroyed blocks
                    this.itemStack.setDamageValue(this.itemStack.getDamageValue() - 1);
                    // like itemStack.setDamageValue but for the number of times used for the pickaxe
                    this.player.awardStat(Stats.ITEM_USED.get(item), -1);
                }
            }

        }
    }

    private void mineOres(Direction direction, Block startingBlock, BlockPos pos) {
        pos = pos.offset(direction.getNormal());
        this.blockState = this.world.getBlockState(pos);
        Block currentBlock = this.blockState.getBlock();

        //TODO: have a look at itemstack.canHarvestBlock

        // mine Block and get drop(s) + exp
        if (isOreBlock(currentBlock) && (currentBlock == startingBlock) &&
                (this.itemStack.getDamageValue() < this.itemStack.getMaxDamage())) {

            // "mine" block
            this.world.removeBlock(pos, false);

            Block.dropResources(this.blockState, (Level) this.world, pos, null, this.player, this.itemStack);
            int exp = currentBlock.getExpDrop(this.blockState, this.world, RandomSource.create(), pos, this.fortune,
                    this.silktouch);

            currentBlock.popExperience((ServerLevel) this.world, pos, exp);

            // damage the pickaxe by 1
            this.itemStack.setDamageValue(this.itemStack.getDamageValue() + 1);

            // add + 1 of times used for pickaxe
            this.player.awardStat(Stats.ITEM_USED.get(this.item));

            // attempt to mine neighbour blocks
            mineOres(Direction.DOWN, startingBlock, pos);
            mineOres(Direction.UP, startingBlock, pos);
            mineOres(Direction.NORTH, startingBlock, pos);
            mineOres(Direction.SOUTH, startingBlock, pos);
            mineOres(Direction.WEST, startingBlock, pos);
            mineOres(Direction.EAST, startingBlock, pos);
        }
    }

    private void handleEnchantment() {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(this.itemStack);
        this.silktouch = enchantments.getOrDefault(Enchantments.SILK_TOUCH, 0);
        this.fortune = enchantments.getOrDefault(Enchantments.BLOCK_FORTUNE, 0);
    }

    private boolean isOreBlock(Block block) {
        return ((block instanceof DropExperienceBlock) || block instanceof RedStoneOreBlock);
        /*return (   block == Blocks.GOLD_ORE
                || block == Blocks.COAL_ORE
                || block == Blocks.IRON_ORE
                || block == Blocks.REDSTONE_ORE
                || block == Blocks.DIAMOND_ORE
                || block == Blocks.EMERALD_ORE
                || block == Blocks.NETHER_QUARTZ_ORE
                || block == Blocks.LAPIS_ORE
                || block == Blocks.NETHER_GOLD_ORE
                || block == Blocks.COPPER_ORE
                || block == Blocks.ANCIENT_DEBRIS
        );*/
    }

    private boolean hasAnyNeighbourOfSameType(BlockPos blockPos, Block minedBlock) {
        for (Direction direction : Direction.values()) {
            BlockPos pos = new BlockPos(
                    blockPos.getX() + direction.getStepX()
                    , blockPos.getY() + direction.getStepY()
                    , blockPos.getZ() + direction.getStepZ());
            Block block = this.world.getBlockState(pos).getBlock();
            if (block == minedBlock && block != Blocks.AIR) {
                return true;
            }
        }
        return false;
    }

}
