package miniplc0java.navm.instruction;

import miniplc0java.navm.Assembler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class InstructionU32 extends Instruction{

    //4字节参数
    private int param;

    public InstructionU32(InstructionType instructionType, int param) {
        super(instructionType);
        this.param = param;
    }


    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public String debugString() {
        return this.getInstructionType().toString()+"("+this.getParam()+")";
    }

    @Override
    public void toAssemble(List<Byte> byteList) throws IOException {
        byte by = Assembler.char2Byte(this.getOpCode());
        byteList.add(by);
        byte []bytes = Assembler.int2Byte(this.getParam());
        for(int i=0;i<bytes.length;i++ ){
            byteList.add(bytes[i]);
        }
    }
}
