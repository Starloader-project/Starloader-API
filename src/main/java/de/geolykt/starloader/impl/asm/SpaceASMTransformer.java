package de.geolykt.starloader.impl.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minestom.server.extras.selfmodification.CodeModifier;

import de.geolykt.starloader.DebugNagException;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.empire.EmpireCollapseEvent;
import de.geolykt.starloader.api.event.empire.EmpireCollapseEvent.EmpireCollapseCause;
import de.geolykt.starloader.api.event.lifecycle.GalaxySavingEndEvent;
import de.geolykt.starloader.api.event.lifecycle.GalaxySavingEvent;
import de.geolykt.starloader.api.event.lifecycle.GraphicalTickEvent;
import de.geolykt.starloader.api.event.lifecycle.LogicalTickEvent;
import de.geolykt.starloader.impl.GalimulatorImplementation;

import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.fn;

/**
 * Transformers targeting the Space class.
 */
public class SpaceASMTransformer extends CodeModifier {

    /**
     * The internal name of the {@link ActiveEmpire} class.
     */
    private static final String ACTIVE_EMPIRE_CLASS = "de/geolykt/starloader/api/empire/ActiveEmpire";

    /**
     * The logger object that should be used used throughout this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceASMTransformer.class);

    /**
     * The internal name of the class that this transformer seeks to modify.
     */
    private static final String SPACE_CLASS = "snoddasmannen/galimulator/Space";

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

    public static final void save(String cause, String location) {
        Space.J = "Saving galaxy: " + cause;
        LOGGER.info("Saving state to disk.");
        GalimulatorImplementation.suppressSaveEvent = true;
        EventManager.handleEvent(new GalaxySavingEvent(NullUtils.requireNotNull(cause, "cause is null"), NullUtils.requireNotNull(location, "location is null"), true));
        try (FileOutputStream fos = new FileOutputStream(new File(location))) {
            Galimulator.saveGameState(fos);
        } catch (IOException e) {
            LOGGER.error("IO Error while saving the state of the game", e);
        } catch (Throwable e) {
            LOGGER.error("Error while saving the state of the game", e);
        } finally {
            EventManager.handleEvent(new GalaxySavingEndEvent(NullUtils.requireNotNull(location, "location is null"), true));
            GalimulatorImplementation.suppressSaveEvent = false;
        }
        for (fn var1 : Space.u) {
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
        while (currentInsn != null) {
            if (currentInsn instanceof FieldInsnNode) {
                FieldInsnNode yField = (FieldInsnNode) currentInsn;
                currentInsn = currentInsn.getNext();
                if (!yField.owner.equals(SPACE_CLASS) || !yField.name.equals("y") || currentInsn.getOpcode() != Opcodes.ICONST_2) {
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
                while (lastNode.getOpcode() != Opcodes.RETURN) {
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
    public @Nullable String getNamespace() {
        return "snoddasmannen.galimulator.Space";
    }

    @Override
    public boolean transform(ClassNode source) {
        if (source.name.equals(SPACE_CLASS)) {
            for (MethodNode method : source.methods) {
                if (method.name.equals("f") && method.desc.equals("(Lsnoddasmannen/galimulator/Empire;)V")) {
                    addEmpireCollapseListener(method);
                } else if (method.name.equals("u") && method.desc.equals("()V")) {
                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "graphicalTickPre", "()V"));
                    method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "graphicalTickPost", "()V"));
                } else if (method.name.equals("B") && method.desc.equals("()V")) {
                    addLogicalListener(method);
                } else if (method.name.equals("b") && method.desc.equals("(Ljava/lang/String;Ljava/lang/String;)V")) {
                    method.instructions.clear();
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORMER_CLASS, "save", "(Ljava/lang/String;Ljava/lang/String;)V"));
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                    method.tryCatchBlocks.clear();
                }
            }
            ClassWriter w = new ClassWriter(0);
            source.accept(w);
            try (FileOutputStream fos = new FileOutputStream("Space.class")) {
                fos.write(w.toByteArray());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
