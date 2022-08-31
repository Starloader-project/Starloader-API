package de.geolykt.starloader.impl.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.DebugNagException;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.empire.EmpireCollapseEvent;
import de.geolykt.starloader.api.event.empire.EmpireCollapseEvent.EmpireCollapseCause;
import de.geolykt.starloader.api.event.lifecycle.GalaxyGeneratedEvent;
import de.geolykt.starloader.api.event.lifecycle.GraphicalTickEvent;
import de.geolykt.starloader.api.event.lifecycle.LogicalTickEvent;
import de.geolykt.starloader.api.serial.SupportedSavegameFormat;
import de.geolykt.starloader.impl.StarplaneReobfuscateReference;
import de.geolykt.starloader.transformers.ASMTransformer;

import snoddasmannen.galimulator.Settings;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.interface_9;

/**
 * Transformers targeting the Space class.
 * Also transforms other classes because we shouldn't have 200 highly specialised ASM transformers.
 * This might be changed once we move to the actual ASM Transformer class instead of the out-dated and deprecated
 * CodeModifier class which is less performant for higher numbers due to it never getting purged
 */
public class SpaceASMTransformer extends ASMTransformer {

    /**
     * The internal name of the {@link ActiveEmpire} class.
     */
    private static final String ACTIVE_EMPIRE_CLASS = "de/geolykt/starloader/api/empire/ActiveEmpire";

    /**
     * The logger object that should be used used throughout this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceASMTransformer.class);

    @StarplaneReobfuscateReference
    @NotNull
    public static String mapModeShowsActorsMethod = "snoddasmannen/galimulator/MapMode$MapModes.getShowsActors()Z";

    /**
     * The remapped name of the "saveSync" method.
     *
     * @since 2.0.0
     * @see StarplaneReobfuscateReference
     */
    @StarplaneReobfuscateReference
    @NotNull
    public static String saveSyncMethod = "snoddasmannen/galimulator/Space.saveSync(Ljava/lang/String;Ljava/lang/String;)V";

    @StarplaneReobfuscateReference
    @NotNull
    public static String gestureListenerClass = "snoddasmannen/galimulator/GalimulatorGestureListener";

    /**
     * The internal name of the class that this transformer seeks to modify.
     */
    private static final String SPACE_CLASS = "snoddasmannen/galimulator/Space";

    /**
     * The remapped name of the "generateGalaxy" method.
     *
     * @since 2.0.0
     * @see StarplaneReobfuscateReference
     */
    @StarplaneReobfuscateReference
    @NotNull
    public static String generateGalaxyMethod = "snoddasmannen/galimulator/Space.generateGalaxy(ILsnoddasmannen/galimulator/MapData;)V";

    /**
     * The remapped name of the "Space.draw" method.
     *
     * @since 2.0.0
     * @see StarplaneReobfuscateReference
     */
    @StarplaneReobfuscateReference
    @NotNull
    public static String spaceDrawMethod = "snoddasmannen/galimulator/Space.draw(Lsnoddasmannen/galimulator/rendersystem/RenderCache;)V";

    @StarplaneReobfuscateReference
    @NotNull
    public static String starRenderOverlayMethod = "snoddasmannen/galimulator/Star.renderRegion()V";

    @StarplaneReobfuscateReference
    @NotNull
    public static String tickCountField = "snoddasmannen/galimulator/Space.tickCount I";

    /**
     * The remapped name of the "tick" method.
     *
     * @since 2.0.0
     * @see StarplaneReobfuscateReference
     */
    @StarplaneReobfuscateReference
    @NotNull
    public static String tickMethod = "snoddasmannen/galimulator/Space.tick()I";

    /**
     * The internal name of the class you are viewing right now right here.
     */
    private static final String TRANSFORMER_CLASS = "de/geolykt/starloader/impl/asm/SpaceASMTransformer";

    /**
     * Emits the {@link EmpireCollapseEvent}. A call to this method is automatically injected by the {@link #addEmpireCollapseListener(MethodNode)} method.
     *
     * @param empire The empire that collapsed.
     * @return True if the collapse should be cancelled
     */
    public static final boolean emitCollapseEvent(ActiveEmpire empire) {
        EmpireCollapseEvent e = new EmpireCollapseEvent(empire, empire.getStarCount() == 0 ? EmpireCollapseCause.NO_STARS : EmpireCollapseCause.UNKNOWN);
        if (e.getCause() == EmpireCollapseCause.UNKNOWN) {
            // I have never seen this nag yet, so it can likely be assumed that the assumption is right.
            DebugNagException.nag("This method is thought to be only used for GC after a empire has no stars!");
        }
        EventManager.handleEvent(e);
        return e.isCancelled();
    }

    /**
     * Called at the end of the render method.
     */
    public static final void graphicalTickPost() {
        EventManager.handleEvent(new GraphicalTickEvent(GraphicalTickEvent.Phase.POST));
    }

    /**
     * Called at the beginning of the render method.
     */
    public static final void graphicalTickPre() {
        EventManager.handleEvent(new GraphicalTickEvent(GraphicalTickEvent.Phase.PRE));
    }

    /**
     * Called at the very beginning of the global tick method.
     */
    public static final void logicalTickEarly() {
        EventManager.handleEvent(new LogicalTickEvent(LogicalTickEvent.Phase.PRE_GRAPHICAL));
    }

    /**
     * Called at the end of the global tick method.
     */
    public static final void logicalTickPost() {
        EventManager.handleEvent(new LogicalTickEvent(LogicalTickEvent.Phase.POST));
    }

    /**
     * Called at the beginning of the pause-sensitive portion of the global tick method.
     */
    public static final void logicalTickPre() {
        EventManager.handleEvent(new LogicalTickEvent(LogicalTickEvent.Phase.PRE_LOGICAL));
    }

    public static final void generateGalaxy(boolean finished) {
        if (finished) {
            EventManager.handleEvent(new GalaxyGeneratedEvent());
        } else {
            DebugNagException.nag();
        }
    }

    public static final void save(String cause, String location) {
        Space.backgroundTaskDescription = "Saving galaxy: " + cause;
        LOGGER.info("Saving state to disk.");

        try (FileOutputStream fos = new FileOutputStream(new File(location))) {
            Galimulator.getSavegameFormat(SupportedSavegameFormat.SLAPI_BOILERPLATE).saveGameState(fos, cause, location);
        } catch (IOException e) {
            LOGGER.error("IO Error while saving the state of the game", e);
        } catch (Throwable e) {
            if (e instanceof OutOfMemoryError) {
                System.gc();
            }
            LOGGER.error("Error while saving the state of the game", e);
            if (e instanceof ThreadDeath) {
                throw e;
            }
        } finally {
            Settings.b("StartedLoading", false);
        }

        // No idea what this does.
        // Apparently there is no implementation of "interface_9" so we cannot really know.
        // Upcoming feature perhaps?
        // Apparently this has been there for quite a while too
        for (interface_9 var1 : Space.w) {
            var1.f();
        }
    }

    /**
     * Adds the callbacks that are responsible of emitting the {@link EmpireCollapseEvent}.
     * Please beware that this method does not do any sanity checks, it will blindly transform the given method node.
     *
     * @param source The method node to transform
     */
    private void addEmpireCollapseListener(MethodNode source) {
        AbstractInsnNode loadvar = new VarInsnNode(Opcodes.ALOAD, 0); // We inject into a static method, variable 0 corresponds to the first parameter
        AbstractInsnNode checkcast = new TypeInsnNode(Opcodes.CHECKCAST, ACTIVE_EMPIRE_CLASS); // The parameter is the snoddasmannen/galimulator/Empire, which can be safely casted to ActiveEmpire
        AbstractInsnNode callMethod = new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "emitCollapseEvent", "(L" + ACTIVE_EMPIRE_CLASS + ";)Z");
        LabelNode skipReturnLabel = new LabelNode();
        AbstractInsnNode returnInsn = new InsnNode(Opcodes.RETURN);
        AbstractInsnNode condSkipReturn = new JumpInsnNode(Opcodes.IFEQ, skipReturnLabel);
        // Keep in mind of reversed order
        source.instructions.insert(skipReturnLabel);
        source.instructions.insert(returnInsn);
        source.instructions.insert(condSkipReturn);
        source.instructions.insert(callMethod);
        source.instructions.insert(checkcast);
        source.instructions.insert(loadvar);
    }

    /**
     * Adds the required bytecode so a {@link LogicalTickEvent} is thrown whenever needed.
     * Please beware that this method does not do any sanity checks, it will mostly blindly transform the given method node.
     *
     * @param method The method to transform
     */
    private void addLogicalListener(MethodNode method) {
        AbstractInsnNode currentInsn = method.instructions.getFirst();
        String tickCountFieldName = tickCountField.split("[\\. ]")[1];
        while (currentInsn != null) {
            if (currentInsn instanceof FieldInsnNode) {
                FieldInsnNode yField = (FieldInsnNode) currentInsn;
                currentInsn = currentInsn.getNext();
                if (!yField.owner.equals(SPACE_CLASS) || !yField.name.equals(tickCountFieldName) || currentInsn.getOpcode() != Opcodes.ICONST_2) {
                    continue;
                }
                currentInsn = currentInsn.getNext();
                if (currentInsn.getOpcode() != Opcodes.IREM) {
                    continue;
                }
                currentInsn = currentInsn.getNext();
                if (!(currentInsn instanceof JumpInsnNode)) {
                    continue;
                }
                JumpInsnNode jumpToPOI = (JumpInsnNode) currentInsn;
                while (currentInsn != jumpToPOI.label) {
                    currentInsn = currentInsn.getNext();
                }
                // WARNING: this is some seriously dangerous assumptions.
                AbstractInsnNode lastNode = method.instructions.getLast();
                Objects.requireNonNull(lastNode);
                while (lastNode.getOpcode() != Opcodes.IRETURN && lastNode.getOpcode() != Opcodes.RETURN) {
                    lastNode = lastNode.getPrevious();
                }
                method.instructions.insert(currentInsn, new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "logicalTickPre", "()V"));
                method.instructions.insertBefore(lastNode, new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "logicalTickPost", "()V"));
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "logicalTickEarly", "()V"));
            }
            currentInsn = currentInsn.getNext();
        }
    }

    @Override
    public boolean accept(@NotNull ClassNode source) {
        if (source.name.equals(SPACE_CLASS)) {
            String generateGalaxyMethodName = generateGalaxyMethod.split("[\\.\\(]", 3)[1];
            String tickMethodName = tickMethod.split("[\\.\\(]", 3)[1];
            String tickMethodDesc = '(' + tickMethod.split("[\\.\\(]", 3)[2];
            String saveSyncMethodName = saveSyncMethod.split("[\\.\\(]", 3)[1];
            String spaceDrawMethodName = spaceDrawMethod.split("[\\.\\(]", 3)[1];
            String spaceDrawMethodDesc = '(' + spaceDrawMethod.split("[\\.\\(]", 3)[2];

            boolean foundTickMethod = false;
            boolean foundEmpireCollapseMethod = false;
            boolean foundSaveSyncMethod = false;
            boolean foundSpaceDrawMethod = false;
            boolean foundSaveGalaxyMethodName = false;

            for (MethodNode method : source.methods) {
                if (method.name.equals("f") && method.desc.equals("(Lsnoddasmannen/galimulator/Empire;)V")) {
                    addEmpireCollapseListener(method);
                    foundEmpireCollapseMethod = true;
                } else if (method.name.equals(spaceDrawMethodName) && method.desc.equals(spaceDrawMethodDesc)) {
                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "graphicalTickPre", "()V"));
                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "graphicalTickPost", "()V"));
                    foundSpaceDrawMethod = true;
                } else if (method.name.equals(tickMethodName) && method.desc.equals(tickMethodDesc)) {
                    addLogicalListener(method);
                    foundTickMethod = true;
                } else if (method.name.equals(saveSyncMethodName) && method.desc.equals("(Ljava/lang/String;Ljava/lang/String;)V")) {
                    method.instructions.clear();
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "save", "(Ljava/lang/String;Ljava/lang/String;)V"));
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                    method.tryCatchBlocks.clear();
                    foundSaveSyncMethod = true;
                } else if (method.name.equals(generateGalaxyMethodName) && method.desc.equals("(ILsnoddasmannen/galimulator/MapData;)V")) {
                    AbstractInsnNode returnInsn = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.RETURN) {
                            if (returnInsn != null) {
                                throw new IllegalStateException("Bytecode is no longer laid out as expected");
                            }
                            returnInsn = insn;
                        }
                    }
                    if (returnInsn == null) {
                        throw new IllegalStateException("There is no return opcode in this method. Is this even valid java?");
                    }
                    MethodInsnNode insn = new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "generateGalaxy", "(Z)V");
                    method.instructions.insertBefore(returnInsn, new InsnNode(Opcodes.ICONST_1)); // load true into the stack
                    method.instructions.insertBefore(returnInsn, insn);
                    foundSaveGalaxyMethodName = true;
                }
            }

            boolean[] foundStuff = new boolean[] {
                    foundEmpireCollapseMethod,
                    foundSaveGalaxyMethodName,
                    foundSaveSyncMethod,
                    foundSpaceDrawMethod,
                    foundTickMethod
            };
            for (boolean x : foundStuff) {
                if (!x) {
                    throw new AssertionError("Unable to transform class! Found stuff: " + Arrays.toString(foundStuff));
                }
            }
            return true;
        } else if (source.name.equals("snoddasmannen/galimulator/factions/Faction")) {
            for (MethodNode method : source.methods) {
                if (method.name.equals("d") && method.desc.equals("()V")) {
                    final String factionRebelEvent = "de/geolykt/starloader/api/event/empire/factions/FactionRebelEvent";
                    LabelNode skipLabel = new LabelNode();
                    InsnList injectedInstructions = new InsnList();
                    injectedInstructions.add(new TypeInsnNode(Opcodes.NEW, factionRebelEvent));
                    injectedInstructions.add(new InsnNode(Opcodes.DUP));
                    injectedInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // ALOAD THIS
                    injectedInstructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, factionRebelEvent, "<init>", "(Lde/geolykt/starloader/api/empire/Faction;)V"));
                    injectedInstructions.add(new VarInsnNode(Opcodes.ASTORE, 1)); // ASTORE 1
                    injectedInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    injectedInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/geolykt/starloader/api/event/EventManager", "handleEvent", "(Lde/geolykt/starloader/api/event/Event;)V"));
                    injectedInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    injectedInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, factionRebelEvent, "isCancelled", "()Z"));
                    injectedInstructions.add(new JumpInsnNode(Opcodes.IFEQ, skipLabel));
                    injectedInstructions.add(new InsnNode(Opcodes.RETURN));
                    injectedInstructions.add(skipLabel);
                    method.instructions.insert(injectedInstructions);
                }
            }
            return true;
        } else if (source.name.equals("snoddasmannen/galimulator/Star")) {
            String methodName = starRenderOverlayMethod.split("[\\.\\(]", 3)[1];
            boolean transformed = false;
            for (MethodNode method : source.methods) {
                if (method.desc.equals("()V") && method.name.equals(methodName)) {
                    if (transformed) {
                        DebugNagException.nag("Duplicate method?");
                        throw new IllegalStateException("Unexpected error while transforming class: Duplicate method");
                    }
                    LabelNode jumpTarget = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.GOTO) {
                            JumpInsnNode jump = (JumpInsnNode) insn;
                            jumpTarget = jump.label;
                            break;
                        }
                    }
                    if (jumpTarget == null) {
                        throw new NullPointerException(); // Not going to happen
                    }
                    LabelNode lastLabelNode = null;
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if (methodInsn.owner.equals("snoddasmannen/galimulator/MapMode")
                                    && methodInsn.desc.equals("()Lsnoddasmannen/galimulator/MapMode$MapModes;")) {
                                // Probably getCurrentMode
                                MethodInsnNode insertedInsn = new MethodInsnNode(Opcodes.INVOKESTATIC, "de/geolykt/starloader/impl/asm/StarCallbacks", "setPolygonColor", "(Lsnoddasmannen/galimulator/Star;)Z");
                                if (lastLabelNode == null) {
                                    throw new IllegalStateException("No last label node?");
                                }
                                method.instructions.insertBefore(lastLabelNode, new VarInsnNode(Opcodes.ALOAD, 0));
                                method.instructions.insertBefore(lastLabelNode, insertedInsn);
                                method.instructions.insertBefore(lastLabelNode, new JumpInsnNode(Opcodes.IFNE, jumpTarget));
                                transformed = true;
                                break;
                            }
                        } else if (insn instanceof LabelNode) {
                            lastLabelNode = (LabelNode) insn;
                        }
                    }
                    if (!transformed) {
                        DebugNagException.nag("Cannot resolve instruction.");
                        throw new IllegalStateException("Unexpected error while transforming class: Cannot resolve instruction");
                    }
                }
            }
            if (!transformed) {
                DebugNagException.nag("Cannot resolve method: " + methodName + " (" + starRenderOverlayMethod + ")");
                throw new IllegalStateException("Unexpected error while transforming class: Cannot resolve method");
            }
        } else if (source.name.equals(gestureListenerClass)) {
            boolean transformed = false;
            for (MethodNode method : source.methods) {
                if (!method.name.equals("tap") || !method.desc.equals("(FFII)Z")) {
                    continue;
                }
                String showActorsMethodname = mapModeShowsActorsMethod.split("[\\.\\(]", 3)[1];
                MethodInsnNode getActorsCall = null;
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (!methodInsn.name.equals(showActorsMethodname) || !methodInsn.owner.equals("snoddasmannen/galimulator/MapMode$MapModes")) {
                            continue;
                        }
                        getActorsCall = methodInsn;
                    }
                }
                if (getActorsCall == null) {
                    DebugNagException.nag("Ex2");
                    throw new IllegalStateException("Ex2");
                }
                AbstractInsnNode nextInsn = getActorsCall.getNext();
                VarInsnNode lastLoad = null;
                LineNumberNode lastLineInsn = null;
                while (nextInsn != null && (nextInsn.getOpcode() != Opcodes.INVOKESTATIC || !((MethodInsnNode) nextInsn).desc.equals("(FFDLsnoddasmannen/galimulator/Empire;)Lsnoddasmannen/galimulator/Star;"))) {
                    if (nextInsn.getOpcode() == Opcodes.ALOAD) {
                        lastLoad = (VarInsnNode) nextInsn;
                    } else if (nextInsn instanceof LineNumberNode) {
                        lastLineInsn = (LineNumberNode) nextInsn;
                    }
                    nextInsn = nextInsn.getNext();
                }
                if (nextInsn == null || lastLoad == null) {
                    DebugNagException.nag("Ex1");
                    throw new IllegalStateException("Ex1");
                }
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, lastLoad.var));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/geolykt/starloader/impl/asm/StarCallbacks", "onLeftClick", "(Lcom/badlogic/gdx/math/Vector3;)Z"));
                LabelNode label = new LabelNode();
                insnList.add(new JumpInsnNode(Opcodes.IFEQ, label));
                insnList.add(new InsnNode(Opcodes.ICONST_1));
                insnList.add(new InsnNode(Opcodes.IRETURN));
                insnList.add(label);
                method.instructions.insert(lastLineInsn, insnList);
                transformed = true;
            }
            if (!transformed) {
                DebugNagException.nag();
                throw new IllegalStateException("Cannot transform method for some reason");
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidTraget(@NotNull String internalName) {
        if (!internalName.startsWith("sno")) {
            return false;
        }
        String fqn = internalName.replace('.', '/');
        return fqn.equals(SPACE_CLASS)
                || fqn.equals("snoddasmannen/galimulator/factions/Faction")
                || fqn.equals("snoddasmannen/galimulator/Star")
                || fqn.equals(gestureListenerClass);
    }
}
