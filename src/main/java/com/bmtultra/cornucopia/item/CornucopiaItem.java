package com.pizza573.cornucopia.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// todo 实现食物选择逻辑
public class CornucopiaItem extends Item
{
    // static 变量，所有对象共享同一个静态字段值，示例：计数器、配置常量等。
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);// 蓝色
    private static final int ITEM_NAME_COLOR = Mth.color(0.133f, 0.545f, 0.133f);// 暗绿色
    private static final int MAX_WEIGHT = 128; // 1.20.1 不做附魔
    private static final int REDUCE_TIME = 6; // 0.3s
    // todo 使用 suitableFood record 类替代
    private int suitableFoodIndex = 0;
    private ItemStack suitableFood = ItemStack.EMPTY;

    public CornucopiaItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack)
    {
        return Component.translatable(super.getName(stack).getString()).withStyle(style -> style.withColor(ITEM_NAME_COLOR));
    }

    // 供物品属性weight使用，类似boson的magicIngot，“使物品能够动态的切换贴图”
    public static float getWeightDisplay(ItemStack stack)
    {
        return (float) getContentWeight(stack) / MAX_WEIGHT;
    }

    // 存储相关基本逻辑 ***start***
    private static int add(ItemStack pBundleStack, ItemStack pInsertedStack)
    {
        if (!pInsertedStack.isEmpty() && pInsertedStack.getItem().canFitInsideContainerItems()) {
            CompoundTag compoundtag = pBundleStack.getOrCreateTag();
            if (!compoundtag.contains("Items")) {
                compoundtag.put("Items", new ListTag());
            }

            int i = getContentWeight(pBundleStack);// 获取容器内物品总权重
            int j = getWeight(pInsertedStack);// 获取插入单个物品的权重
            int k = Math.min(pInsertedStack.getCount(), (MAX_WEIGHT - i) / j);// 此次插入物品的数量
            if (k == 0) {
                return 0;
            } else {
                ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
                Optional<CompoundTag> optional = getMatchingItem(pInsertedStack, listtag);// 寻找拥有相同tag的Item（匹配的itemStack）
                if (optional.isPresent()) {// 找到匹配的物品
                    CompoundTag compoundTag1 = optional.get();// compoundTag1
                    ItemStack itemstack = ItemStack.of(compoundTag1);// 匹配的itemStack
                    if (k + itemstack.getCount() > 64) {
                        int increment = 64 - itemstack.getCount();
                        int rest = k - increment;
                        // grow
                        itemstack.grow(increment);
                        itemstack.save(compoundTag1);
                        listtag.remove(compoundTag1);
                        listtag.add(0, compoundTag1);
                        // new CompoundTag
                        ItemStack itemstack1 = pInsertedStack.copyWithCount(rest);
                        listtag.add(0, itemstack1.save(new CompoundTag()));
                    } else if (k + itemstack.getCount() <= 64) {
                        itemstack.grow(k);
                        itemstack.save(compoundTag1);
                        listtag.remove(compoundTag1);
                        listtag.add(0, compoundTag1);
                    }
                } else {// 未找到匹配的物品
                    ItemStack itemstack1 = pInsertedStack.copyWithCount(k);
                    CompoundTag compoundTag2 = new CompoundTag();
                    itemstack1.save(compoundTag2);
                    listtag.add(0, compoundTag2);
                }

                return k;
            }
        } else {
            return 0;
        }
    }

    private static Optional<CompoundTag> getMatchingItem(ItemStack pStack, ListTag pList)
    {
        return pList.stream().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).filter((compoundTag) -> {
            return ItemStack.isSameItemSameTags(ItemStack.of(compoundTag), pStack);
        }).findFirst();
    }

    private static int getWeight(ItemStack pStack)
    {
        return 64 / pStack.getMaxStackSize();
    }

    private static int getContentWeight(ItemStack pStack)
    {
        return getContents(pStack).mapToInt((itemStack) -> getWeight(itemStack) * itemStack.getCount()).sum();
    }

    private static Optional<ItemStack> removeOneStack(ItemStack pStack)// 移除一组
    {
        CompoundTag compoundtag = pStack.getOrCreateTag();
        if (!compoundtag.contains("Items")) {
            return Optional.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
            if (listtag.isEmpty()) {
                return Optional.empty();
            } else {
                // 进行移除逻辑
                CompoundTag compoundtag1 = listtag.getCompound(0);
                ItemStack itemstack = ItemStack.of(compoundtag1);
                listtag.remove(0);
                if (listtag.isEmpty()) {
                    pStack.removeTagKey("Items");
                }

                return Optional.of(itemstack);
            }
        }
    }

    private static Optional<ItemStack> removeOneItem(ItemStack pStack, int index)// 按下标移除一个物品
    {
        CompoundTag compoundtag = pStack.getOrCreateTag();
        if (!compoundtag.contains("Items")) {
            return Optional.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
            if (listtag.isEmpty()) {
                return Optional.empty();
            } else {// 进行移除逻辑
                // 获取物品同时移除物品
                ItemStack restItem = ItemStack.of(listtag.getCompound(index));
                listtag.remove(index);
                // 物品数量-1；剩余物品&移除物品
                ItemStack removedItem = restItem.split(1);
                // 检查剩余物品数量;有余就放回
                if (restItem.getCount() != 0) {
                    listtag.add(index, restItem.save(new CompoundTag()));
                }
                if (listtag.isEmpty()) {
                    pStack.removeTagKey("Items");
                }

                return Optional.of(removedItem);// 返回被移除的物品
            }
        }
    }

    private static Optional<ItemStack> getItemStackCopy(ItemStack pStack, int index)
    {
        CompoundTag compoundtag = pStack.getOrCreateTag();
        if (!compoundtag.contains("Items")) {
            return Optional.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
            if (listtag.isEmpty()) {
                return Optional.empty();
            } else {// 执行逻辑
                ItemStack itemStack = ItemStack.of(listtag.getCompound(index));

                return Optional.of(itemStack);// 返回被移除的物品
            }
        }
    }

    private static Stream<ItemStack> getContents(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag == null) {
            return Stream.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
            return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }
    // 存储相关基本逻辑 ***end***

    private void updateSuitableFood(ItemStack cornucopia)
    {
        getItemStackCopy(cornucopia, 0).ifPresentOrElse(itemStack -> suitableFood = itemStack, () -> suitableFood = ItemStack.EMPTY);
    }

    public boolean overrideStackedOnOther(ItemStack pStack, @NotNull Slot pSlot, @NotNull ClickAction pAction, @NotNull Player pPlayer)
    {
        if (pStack.getCount() != 1 || pAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = pSlot.getItem();
            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(pPlayer);
                removeOneStack(pStack).ifPresent((itemStack) -> {
                    add(pStack, pSlot.safeInsert(itemStack));
                });
            } else if (itemstack.getItem().canFitInsideContainerItems() && itemstack.isEdible()) {// 食物判断
                int i = (MAX_WEIGHT - getContentWeight(pStack)) / getWeight(itemstack);
                int j = add(pStack, pSlot.safeTake(itemstack.getCount(), i, pPlayer));
                if (j > 0) {
                    this.playInsertSound(pPlayer);
                }
            }

            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pStack, @NotNull ItemStack pOther, @NotNull Slot pSlot, @NotNull ClickAction pAction, @NotNull Player pPlayer, @NotNull SlotAccess pAccess)
    {
        if (pStack.getCount() != 1) return false;
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            if (pOther.isEmpty()) {
                removeOneStack(pStack).ifPresent((itemStack) -> {
                    this.playRemoveOneSound(pPlayer);
                    pAccess.set(itemStack);
                });
            } else if (pOther.isEdible()) {// 食物判断
                int i = add(pStack, pOther);
                if (i > 0) {
                    this.playInsertSound(pPlayer);
                    pOther.shrink(i);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand)
    {
        ItemStack cornucopia = pPlayer.getItemInHand(pUsedHand);
        CompoundTag compoundtag = cornucopia.getOrCreateTag();
        updateSuitableFood(cornucopia);

        if (!compoundtag.contains("Items")) {
            return InteractionResultHolder.pass(cornucopia);
        } else {
            ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
            if (listtag.isEmpty()) {
                return InteractionResultHolder.pass(cornucopia);
            } else if (pPlayer.getFoodData().needsFood() || Objects.requireNonNull(suitableFood.getFoodProperties(pPlayer)).canAlwaysEat()) {
                pPlayer.startUsingItem(pUsedHand);
                return InteractionResultHolder.consume(cornucopia);
            }
        }

        return InteractionResultHolder.fail(cornucopia);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity)
    {
        // 创建食物的副本用于测试消费
        ItemStack foodCopy = suitableFood.copy();
        ItemStack originalFood = suitableFood.copy();

        // 调用食物的finishUsingItem方法
        ItemStack consumedResult = foodCopy.getItem().finishUsingItem(foodCopy, level, livingEntity);

        // 检查食物是否真的被消费了
        // 如果返回的物品与原物品相同且数量没有减少，说明是无限食物（如永恒牛排）
        boolean shouldConsume = true;
        if (ItemStack.isSameItemSameTags(originalFood, consumedResult) &&
            consumedResult.getCount() >= originalFood.getCount()) {
            // 这是无限食物，不应该从容器中移除
            shouldConsume = false;
        }

        // 只有当食物真的被消费时，才从容器中移除
        if (shouldConsume) {
            removeOneItem(stack, 0);

            // 如果消费后产生了副产品（如空碗），给玩家
            if (!consumedResult.isEmpty() &&
                !ItemStack.isSameItemSameTags(originalFood, consumedResult) &&
                livingEntity instanceof Player player) {
                if (!player.getInventory().add(consumedResult)) {
                    // 背包满了就掉落
                    player.drop(consumedResult, false);
                }
            }
        } else {
            // 对于无限食物，直接调用原始食物的finishUsingItem来应用效果
            suitableFood.getItem().finishUsingItem(suitableFood, level, livingEntity);
        }

        return stack;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack)
    {
        return suitableFood.getItem().getUseAnimation(suitableFood);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack cornucopia)
    {
        int useDuration = suitableFood.getUseDuration();
        System.out.println("suitableFood" + suitableFood);
        System.out.println("useDuration:" + useDuration);
        return useDuration >= REDUCE_TIME ? useDuration - REDUCE_TIME : useDuration;
    }

    public void appendHoverText(@NotNull ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced)
    {
        pTooltipComponents.add(Component.translatable("item.minecraft.bundle.fullness", getContentWeight(pStack), 128).withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("item.minecraft.cornucopia.description").withStyle(style -> style.withColor(ITEM_NAME_COLOR)));
        }
//        pTooltipComponents.add(Component.translatable("alpha version, it's not stable and Dysfunctional").withStyle(ChatFormatting.RED));
    }

    public boolean isBarVisible(@NotNull ItemStack pStack)
    {
        return getContentWeight(pStack) > 0;
    }

    public int getBarWidth(@NotNull ItemStack pStack)
    {
        return Math.min(1 + 12 * getContentWeight(pStack) / MAX_WEIGHT, 13);
    }

    public int getBarColor(@NotNull ItemStack pStack)
    {
        return BAR_COLOR;
    }

    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack pStack)
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        getContents(pStack).forEach(nonnulllist::add);
        return Optional.of(new BundleTooltip(nonnulllist, getContentWeight(pStack)));
    }


    // 不能被放入背包之类的容器物品，不影响部分容器实体，如：chest
    public boolean canFitInsideContainerItems()
    {
        return false;
    }

    private void playRemoveOneSound(Entity pEntity)
    {
        pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity pEntity)
    {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
