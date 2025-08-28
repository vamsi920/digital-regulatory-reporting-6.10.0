package com.regnosys.drr.dataquality.runner;

import com.regnosys.drr.dataquality.TestPackModifier;
import com.regnosys.drr.dataquality.modifiers.ModifierContext;

import java.util.List;

public interface TestPackModifierFactory {
    List<TestPackModifier> getTestPackModifierModifiers(ModifierContext context);
}
