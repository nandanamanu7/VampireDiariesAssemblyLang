package mars.mips.instructions.customlangs;
import mars.mips.instructions.CustomAssembly;
import mars.mips.hardware.*;
import mars.*;
import mars.util.*;
import mars.mips.instructions.*;


public class TheVampireDiariesAssembly extends CustomAssembly{


    @Override
    public String getName() {
        return "The Vampire Diaries Assembly";
    }


    @Override
    public String getDescription() {
        return "Assembly language to simulate.... and hopefully survive the supernatural town of Mystic Falls";
    }


    @Override
    protected void populate() {
        // summon (li)
        instructionList.add(
                new BasicInstruction("summon $rt, $rs, $imm",
                        "Summons (loads) the immediate value into a 32-bit register $rt (Summon a Character into Mystic Falls with set HP)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "001000 00000 ttttt iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int rt = operands[0];
                                int imm = operands[1] << 16 >> 16;
                                int result = 0 + imm;
                                RegisterFile.updateRegister(rt, result);
                            }
                        }));
        // drain (sub)
        instructionList.add(
                new BasicInstruction("drain $rd, $rs, $rt",
                        "Subtracts the value in $t2 from $t1 and stores the result in $t0 (Subtracts blood/power from one register or the victim)\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt ddddd 00000 100010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands(); // fetches operands
                                int rd = operands[0]; // dest. reg
                                int rsVal = RegisterFile.getValue(operands[1]); // source 1
                                int rtVal = RegisterFile.getValue(operands[2]); // source 2
                                RegisterFile.updateRegister(rd, rsVal - rtVal); // register updated with result of subtraction/draining
                            }
                        }));
        // feed (add)
        instructionList.add(
                new BasicInstruction("feed $rd, $rs, $rt",
                        "Adds the value in $t2 and $t1 and stores the result in $t0 (Adds blood/power to the attacker/vampire)\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt ddddd 00000 100000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int prev = RegisterFile.getValue(operands[1]);
                                int next = RegisterFile.getValue(operands[2]);
                                RegisterFile.updateRegister(operands[0], prev + next); // vals added to simulate feeding/gain HP
                            }
                        }));
        // older (mul)
        instructionList.add(
                new BasicInstruction("older $rd, $rs, $rt",
                        "Multiplies the value in $t2 and $t1 and stores the result in $t0 (Multiplies power by given value if vampire is older (In TVD, older means stronger))\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt ddddd 00000 011000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long prod = (long) RegisterFile.getValue(operands[1]) * (long) RegisterFile.getValue(operands[2]);
                                // Stores 32 bits bc error otherwise/MIPS standard mult uses 32
                                RegisterFile.updateRegister(operands[0], ((int) prod));
                            }
                        }));
        // blood_bag (sw)
        instructionList.add(
                new BasicInstruction("blood_bag $rt, offset($rs)",
                        "Stores a word into memory (Stores blood/power into a blood bag at a specific address, with optional offset)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "101011 sssss rrrrr iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int rt = operands[0]; // source
                                int baseReg = operands[1]; // address reg
                                int offset = operands[2] << 16 >> 16; // extending
                                int address = RegisterFile.getValue(baseReg) + offset;
                                int value = RegisterFile.getValue(rt);
                                if ((address & 0x3) != 0) { // checks for alignment (helps debugging)
                                    throw new ProcessingException(statement,
                                            "Word address is not aligned, doesn't work" + address);
                                }
                                try {
                                    Globals.memory.setWord(address, RegisterFile.getValue(rt));
                                } catch (Exception e) {
                                    throw new ProcessingException(statement, "Sw failed at this address" + address);
                                }
                            }
                        }));
        // drink (lw)
        instructionList.add(
                new BasicInstruction("drink $rt, offset($rs)",
                        "Loads a word from memory (Drink blood/power from a blood bag at a specific address, with optional offset)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "100011 sssss rrrrr iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int address = RegisterFile.getValue(operands[1]) + (operands[2] << 16 >> 16);
                                if ((address & 0x3) != 0) { // checks for alignment (helps debugging)
                                    throw new ProcessingException(statement,
                                            "Word address is not aligned, doesn't work" + address);
                                }
                                try {
                                    RegisterFile.updateRegister(operands[0], Globals.memory.getWord(address)); // try to fetch word from address
                                } catch (Exception e) {
                                    throw new ProcessingException(statement, "Lw failed at this address" + address); // throws exception
                                }
                            }
                        }));
        // fangs (beq)
        instructionList.add(
                new BasicInstruction("fangs $rs,$rt,label",
                        "Branch if equal (Sizes up other vampire to check if equals, picks a fight if they are)\n",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000100 sssss rrrrr iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) == RegisterFile.getValue(operands[1])) { // checks if equal vals in registers
                                    Globals.instructionSet.processBranch(operands[2]); // branch
                                }
                            }
                        }));
        // sprint (bne)
        instructionList.add(
                new BasicInstruction("sprint $rs,$rt,label",
                        "Branch if not equal (Sizes up other vampire to check if not equals, sprints/branches away if they are not)\n",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000101 sssss rrrrr iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) != RegisterFile.getValue(operands[1])) { // if registers not equal
                                    Globals.instructionSet.processBranch(operands[2]); // jumps to the label's address
                                }
                            }
                        }));
        // disappear (j)
        instructionList.add(
                new BasicInstruction("disappear label",
                        "Jump to address/label (Disappears if the situation calls for it, leaves to different address)\n",
                        BasicInstructionFormat.J_FORMAT,
                        "000010 iiiiiiiiiiiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                Globals.instructionSet.processJump(operands[0]); // jumps to label
                            }
                        }));
        // mystic_falls (jal w/o jump)
        instructionList.add(
                new BasicInstruction("mystic_falls $rt,$rs",
                        "Sets location tag of character register to a location register’s value. (Character can roam freely to anywhere that they desire within their town of Mystic Falls)\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 sssss 00000 rrrrr 000010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1])); // Updates registers location tag
                            }
                        }));
        // cure (unique)
        instructionList.add(
                new BasicInstruction("cure $rt,$zero",
                        "Copies the value from $zero to $t1 (Cures a vampire or turns it back to a human, giving them 0 power, as according to the Vampire Diaries)\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 rrrrr 00000 100000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0], 0); // updates register val to 0 to represent 0 HP or human stats
                            }
                        }));
        // daylight_ring (unique)
        instructionList.add(
                new BasicInstruction("daylight_ring $rt, $rs, $imm",
                        "Sets the first flag (bit 0) of the register to be 1, without affecting any other flags (daylight ring is given to the vampire, making immune to daylight)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "001101 rrrrr 00000 0000000000000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int bit = RegisterFile.getValue(operands[0]) | 1; // updates first bit (0) of register to 1
                                RegisterFile.updateRegister(operands[0], bit);
                            }
                        }));
        // daytime (unique)
        instructionList.add(
                new BasicInstruction("daytime $rt, $imm",
                        "Checks if the first flag of the register is 1. \nIf not 1, then subtracts immediate value from vampire.\n (Daytime has risen, if the first flag of the register is 1, the vampire is safe as they have a daylight ring. If not, the vampire gets power subtracted since the sunlight is bad for vampire health)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "001110 rrrrr 00000 iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int rt = RegisterFile.getValue(operands[0]);
                                int imm = operands[1] << 16 >> 16;
                                if ((rt & 1) == 0) {
                                    RegisterFile.updateRegister(operands[0], rt - imm); // if first bit is not one, subtract immediate from rt register
                                }
                            }
                        }));
        // vervain_ring (unique)
        instructionList.add(
                new BasicInstruction("vervain_ring $rt, $rs, $imm",
                        "Sets the second flag (bit 1) of the register to be 1, without affecting any other flags (vervain is given to the human, making immune to vampire compulsion)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "001101 00000 rrrrr 0000000000000010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int bit = RegisterFile.getValue(operands[0]) | 2; // updates second flag of register to 1
                                RegisterFile.updateRegister(operands[0], bit);
                            }
                        }));
        // compel (unique)
        instructionList.add(
                new BasicInstruction("compel $rt, $rs, $imm",
                        "Sets the third flag (bit 2) of the register to be 1, without affecting any other flags if second flag is 0 (Checks if human has vervain protection (bit 1), and if human does not have protection, compels them, changing third flag/compulsion flag, meaning that they are under the influence of a vampire)\n",
                        BasicInstructionFormat.I_FORMAT,
                        "001111 rrrrr rrrrr 0000000000000100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int rs = operands[0];
                                if ((rs & 2) == 0) { // checking if bit pos 2 = 0
                                    RegisterFile.updateRegister(operands[0], rs | 4); // updates third flag (bit 4) of register to flag as compelled if no vervain flag (2)
                                }
                            }
                        }));
        // hybrid (unique)
        instructionList.add(
                new BasicInstruction("hybrid $rt",
                        "Increases value in register by *100. (Hybrid power allows a vampire to have vampire powers, as well as werewolf powers, which allow them to kill another vampire simply by just biting them. This can allow for 100 times the power of a regular vampire, increasing the vampire’s power by 100 times their original power.)\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 rrrrr 00000 011000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int rt = RegisterFile.getValue(operands[0]);
                                RegisterFile.updateRegister(operands[0], rt * 100); // Updates register by *100
                            }
                        }));
        // stake (unique)
        instructionList.add(
                new BasicInstruction("stake $rt,$rs",
                        "Sets flag (bit 8) of the register to be 1, without affecting any other flags signaling the death of the vampire $t0 as they have been staked by character $t1. Syscall prints out a kill and the character (register) names. (Character $t0 is staked, which means that they are killed. Mystic Falls announces that character $t1 has killed character $t0 by stake).\n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss 00000 rrrrr 00000 111110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int bit = RegisterFile.getValue(operands[0]) | 256; // updates eighth bit (2^8). of register to 1 to indicate killed status
                                RegisterFile.updateRegister(operands[0], bit); // Updates register
                                SystemIO.printString("" + operands[0] + " staked by " + operands[1] + "\n");
                            }
                        }));
        // grimoire (unique)
        instructionList.add(
                new BasicInstruction("grimoire $rt, $rs, $imm",
                        "Grimoire is used by a witch to allow a character to spell another character and inflict damage. If damage kills, then the killed flag(bit 8) is set to 1 and syscall prints out the character died by witch spell/grimoire.\n",
                        BasicInstructionFormat.I_FORMAT,
                        "001111 rrrrr sssss iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int imm = operands[2] << 16 >> 16;
                                int charHealth = RegisterFile.getValue(operands[0]) - imm; // subtracts charhealth by damage done by grimoire
                                if (charHealth < 0) {
                                    charHealth = 0; // avoids garbage val if health is negative
                                    charHealth = 0 | 256;
                                    SystemIO.printString("" + operands[0] + " killed by " + operands[1] + " by grimoire spell\n"); // prints if character killed, changes killed flag to 1
                                }
                                RegisterFile.updateRegister(operands[0], charHealth); // Updates register now spelled
                            }
                        }));
        // swap_bodies (unique)
        instructionList.add(
                new BasicInstruction("swap_bodies $rt,$rs",
                        "Swaps all register values from one register to another (Characters switch bodies when this spell is placed upon them) \n",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 rrrrr 00000 sssss 111011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int tempReg = RegisterFile.getValue(operands[0]); // swap (using tempReg)
                                RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                                RegisterFile.updateRegister(operands[1], tempReg);
                            }
                        }));
    }


    // Defining registers and my own themed registers
    public int getRegisterNumber(String name) throws ProcessingException {
        switch (name) {
            case "$cure":
            case "$zero":
                return 0;
            case "$Elena":
                return 8;
            case "$Stefan":
                return 9;
            case "$Damon":
                return 10;
            case "$Bonnie":
                return 11;
            case "$Elijah":
                return 12;
            case "$Caroline":
                return 13;
            case "$Tyler":
                return 14;
            case "$Klaus":
                return 15;
            case "$Katherine":
                return 16;
            case "$Jeremy":
                return 17;
            case "$Salvatore_House":
                return 18;
            case "$Mystic_Grill":
                return 19;
            case "$Woods":
                return 20;
            case "$The_Gilbert_House":
                return 21;
            case "$The_Mikaelson_House":
                return 22;
            case "$Lockwood_Mansion":
                return 23;
            case "$Mystic_Falls_Town_Square":
                return 24;
            case "$Mystic_Falls_High_School":
                return 25;
            case "$Whitmore_College":
                return 26;
            case "$Wickery_Bridge":
                return 27;
            default:
                // regular MIPS registers
                return RegisterFile.getNumber(name);
        }
    }
}


