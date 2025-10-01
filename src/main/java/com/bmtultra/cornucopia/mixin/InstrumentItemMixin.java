package com.pizza573.cornucopia.mixin;

import com.pizza573.cornucopia.Config;
import com.pizza573.cornucopia.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(InstrumentItem.class)
public abstract class InstrumentItemMixin extends Item
{
    // private static final String TAG_ITEMS = "Items";
    @Unique
    private static final int MAX_WEIGHT = 128;
    @Unique
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public InstrumentItemMixin(Item.Properties pProperties)
    {
        super(pProperties);
    }

    // 存储相关基本逻辑 ***start***
    @Unique
    private static int mixin_add(ItemStack pBundleStack, ItemStack pInsertedStack)
    {
        if (!pInsertedStack.isEmpty() && pInsertedStack.getItem().canFitInsideContainerItems()) {
            CompoundTag compoundtag = pBundleStack.getOrCreateTag();
            if (!compoundtag.contains("Items")) {
                compoundtag.put("Items", new ListTag());
            }

            int i = mixin_getContentWeight(pBundleStack);// 获取容器内物品总权重
            int j = mixin_getWeight(pInsertedStack);// 获取插入单个物品的权重
            int k = Math.min(pInsertedStack.getCount(), (MAX_WEIGHT - i) / j);// 此次插入物品的数量
            if (k == 0) {
                return 0;
            } else {
                ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
                Optional<CompoundTag> optional = mixin_getMatchingItem(pInsertedStack, listtag);// 寻找拥有相同tag的Item（匹配的itemStack）
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
                        CompoundTag compoundTag2 = new CompoundTag();
                        itemstack1.save(compoundTag2);
                        listtag.add(0, compoundTag2);
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

    @Unique
    private static Optional<CompoundTag> mixin_getMatchingItem(ItemStack pStack, ListTag pList)
    {
        return pList.stream().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).filter((compoundTag) -> {
            return ItemStack.isSameItemSameTags(ItemStack.of(compoundTag), pStack);
        }).findFirst();
    }

    @Unique
    private static int mixin_getWeight(ItemStack pStack)
    {
        return 64 / pStack.getMaxStackSize();
    }

    @Unique
    private static int mixin_getContentWeight(ItemStack pStack)
    {
        return mixin_getContents(pStack).mapToInt((itemStack) -> mixin_getWeight(itemStack) * itemStack.getCount()).sum();
    }

    @Unique
    private static Optional<ItemStack> mixin_removeOneStack(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getOrCreateTag();
        if (!compoundtag.contains("Items")) {
            return Optional.empty();
        } else {
            ListTag listtag = compoundtag.getList("Items", Tag.TAG_COMPOUND);
            if (listtag.isEmpty()) {
                return Optional.empty();
            } else {
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

    @Unique
    private static Stream<ItemStack> mixin_getContents(ItemStack pStack)
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

    @Override
    public boolean overrideStackedOnOther(ItemStack pStack, @NotNull Slot pSlot, @NotNull ClickAction pAction, @NotNull Player pPlayer)
    {
        if (pStack.getCount() != 1 || pAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = pSlot.getItem();
            if (itemstack.isEmpty()) {
                this.mixin_playRemoveOneSound(pPlayer);
                mixin_removeOneStack(pStack).ifPresent((itemStack) -> {
                    mixin_add(pStack, pSlot.safeInsert(itemStack));
                });
            } else if (itemstack.getItem().canFitInsideContainerItems() && itemstack.getFoodProperties(pPlayer) != null) {// 食物判断
                int i = (MAX_WEIGHT - mixin_getContentWeight(pStack)) / mixin_getWeight(itemstack);
                int j = mixin_add(pStack, pSlot.safeTake(itemstack.getCount(), i, pPlayer));
                if (j > 0) {
                    this.mixin_playInsertSound(pPlayer);
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
                mixin_removeOneStack(pStack).ifPresent((itemStack) -> {
                    this.mixin_playRemoveOneSound(pPlayer);
                    pAccess.set(itemStack);
                });
            } else if (pOther.getFoodProperties(pPlayer) != null) {// 食物判断
                int i = mixin_add(pStack, pOther);
                if (i > 0) {
                    this.mixin_playInsertSound(pPlayer);
                    pOther.shrink(i);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity)
    {
        if (mixin_getContentWeight(stack) == MAX_WEIGHT) {// 装满食物
            ItemStack cornucopiaItemStack = new ItemStack(ModItems.CORNUCOPIA.get());
            if (!Config.COMMON.enableClearFoods.get()) {
                CompoundTag compoundTag = stack.getTag();
                if (compoundTag != null) {
                    ListTag listTag = compoundTag.getList("Items", Tag.TAG_COMPOUND);
                    cornucopiaItemStack.getOrCreateTag().put("Items", listTag);

//                    System.out.println("nbt transformed");
//                    System.out.println(cornucopiaItemStack.getTag());
                }
            }

            return cornucopiaItemStack;
        }

        return stack;
    }

    public boolean isBarVisible(@NotNull ItemStack pStack)
    {
        return mixin_getContentWeight(pStack) > 0;
    }

    public int getBarWidth(@NotNull ItemStack pStack)
    {
        return Math.min(1 + 12 * mixin_getContentWeight(pStack) / MAX_WEIGHT, 13);
    }

    public int getBarColor(@NotNull ItemStack pStack)
    {
        return BAR_COLOR;
    }

    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack pStack)
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        mixin_getContents(pStack).forEach(nonnulllist::add);
        return Optional.of(new BundleTooltip(nonnulllist, mixin_getContentWeight(pStack)));
    }

    @Inject(method = "appendHoverText", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/InstrumentItem;getInstrument(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;"))
    public void appendHoverTextInject(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced, CallbackInfo info)
    {
        pTooltipComponents.add(Component.translatable("item.minecraft.bundle.fullness", mixin_getContentWeight(pStack), 128).withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("item.minecraft.goat_horn.description").withStyle(ChatFormatting.GRAY));
    }

    // 相关音效*2
    @Unique
    private void mixin_playRemoveOneSound(Entity pEntity)
    {
        pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    @Unique
    private void mixin_playInsertSound(Entity pEntity)
    {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }
}
