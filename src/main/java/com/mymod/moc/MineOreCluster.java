package com.mymod.moc;

import java.util.Map;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
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
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.client.event.InputEvent;


@Mod("moc")
public class MineOreCluster {
    private static final Direction[] UPDATE_ORDER = new Direction[]{
            Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP
    };

    private KeyMapping myKey;
    private Player player;
    private LevelAccessor world;
    private ItemStack itemStack;
    private BlockState blockState;
    private Item item;
    private boolean mocIsDisabled = false;
    private int fortune;
    private int silktouch;


    public MineOreCluster() {
        MinecraftForge.EVENT_BUS.register(this);
        initKeyBinding();
    }

    private void initKeyBinding() {
        // For key codes see: net.minecraft.client.util.InputMappings
        // key code 77 = m
        myKey = new KeyMapping("Toggle MOC", 77, "key.categories.misc");
        // Register key, so it will be displayed in the control's menu.
        ClientRegistry.registerKeyBinding(myKey);
    }

    @SubscribeEvent
    public void toggleMOC(InputEvent.KeyInputEvent keyEvent) {
        if (myKey.isDown()) {
            mocIsDisabled = !mocIsDisabled;
            if (mocIsDisabled) {
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("MOC disabled"));
            } else
                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent("MOC enabled"));
        }
    }

    @SubscribeEvent
    public void mineOre(BlockEvent.BreakEvent event) {
        if (!mocIsDisabled) {
            BlockPos blockPos = event.getPos();
            this.blockState = event.getState();
            Block block = this.blockState.getBlock();
            this.player = event.getPlayer();
            this.itemStack = this.player.getMainHandItem();
            item = this.itemStack.getItem();
            this.world = event.getWorld();
            assert Minecraft.getInstance().player != null;
            boolean isCreative = Minecraft.getInstance().player.isCreative(); //playerController.isInCreativeMode();

            // Set values for variables fortune and silktouch
            handleEnchantment();
            //canAttackBlock(BlockState, Level, BlockPos, Player)
            if (isOreBlock(block)
                    && item instanceof net.minecraft.world.item.PickaxeItem
                    && item.canAttackBlock(this.blockState, (Level) this.world, blockPos, this.player)
                    && !isCreative) {
                if (hasSameTypeNeighbour(blockPos, block)) {
                    for (Direction direction : UPDATE_ORDER) {
                        mineOreRecursive(direction, block, blockPos);
                    }
                    // disabling this would calculate x+1 destroyed block instead of x number of destroyed blocks
                    this.itemStack.setDamageValue(this.itemStack.getDamageValue() - 1);
                    // like itemStack.setDamageValue but for the number of times used for the pickaxe
                    player.awardStat(Stats.ITEM_USED.get(item), - 1);
                }
            }

        }
    }

    private void mineOreRecursive(Direction direction, Block startingBlock, BlockPos pos) {
        pos = pos.offset(direction.getNormal()); // pos.offset(direction)
        blockState = this.world.getBlockState(pos);
        Block currentBlock = this.blockState.getBlock();

        //itemstack.canHarvestBlock anschauen

        // mine Block and get drop(s) + exp
        if (isOreBlock(currentBlock) && (currentBlock == startingBlock) && (this.itemStack.getDamageValue() < this.itemStack.getMaxDamage())) {
            // "mine" block
            this.world.removeBlock(pos, false);

            Block.dropResources(this.blockState, (Level) this.world, pos, null, this.player, this.itemStack);
            // get exp
            int exp = currentBlock.getExpDrop(this.blockState, this.world, pos, this.fortune, this.silktouch);

            currentBlock.popExperience((ServerLevel) this.world, pos, exp);

            // damage the pickaxe by 1
            this.itemStack.setDamageValue(this.itemStack.getDamageValue() + 1);

            // add + 1 of times used for pickaxe
            player.awardStat(Stats.ITEM_USED.get(item));

            // attempt to mine neighbor blocks
            mineOreRecursive(Direction.DOWN, startingBlock, pos);
            mineOreRecursive(Direction.UP, startingBlock, pos);
            mineOreRecursive(Direction.NORTH, startingBlock, pos);
            mineOreRecursive(Direction.SOUTH, startingBlock, pos);
            mineOreRecursive(Direction.WEST, startingBlock, pos);
            mineOreRecursive(Direction.EAST, startingBlock, pos);
        }
    }

    private void handleEnchantment(){
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(this.itemStack);
        silktouch = enchantments.getOrDefault(Enchantments.SILK_TOUCH, 0);
        fortune = enchantments.getOrDefault(Enchantments.BLOCK_FORTUNE, 0);
    }

    // TODO: Add future ores
    private boolean isOreBlock(Block block) {
        return ((block instanceof OreBlock) || block instanceof RedStoneOreBlock);
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

    private boolean hasSameTypeNeighbour(BlockPos blockPos, Block minedBlock) {
        for (Direction direction : UPDATE_ORDER) {
            BlockPos pos = new BlockPos(blockPos.getX() + direction.getStepX()
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