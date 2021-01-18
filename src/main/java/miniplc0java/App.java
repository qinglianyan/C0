package miniplc0java;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.Analyser2;
import miniplc0java.analyser.SymbolIter;
import miniplc0java.error.CompileError;
import miniplc0java.instruction.Instruction;
import miniplc0java.navm.OoFile;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {
    public static void main(String[] args) throws CompileError {
        var argparse = buildArgparse();
        Namespace result;
        try {
            result = argparse.parseArgs(args);
        } catch (ArgumentParserException e1) {
            argparse.handleError(e1);
            return;
        }

        var inputFileName = result.getString("input");
        var outputFileName = result.getString("output");
        var debugFileName = "debug.txt";

        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        DataOutputStream output;
//        if (outputFileName.equals("-")) {
//            output = System.out;
//        } else {
            try {
                output = new DataOutputStream(new FileOutputStream(outputFileName));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open output file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
//        }
        
        PrintStream debugOut;
        debugOut=System.out;
        

        Scanner scanner;
        scanner = new Scanner(input);
//        /* 测试输出的时候设置参数“-o - -” */
//        System.out.println("next方式接受：");
//        if(scanner.hasNext()) {
//        	String str=scanner.next();
//        	System.out.println("输出数据："+str);
//        }
        
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);
        var symbolIter=new SymbolIter(tokenizer);
        var analyse=new Analyser2(symbolIter);

        try {
            OoFile ooFile = analyse.analyse();
            ooFile.writeDebug(debugOut);
            List<Byte> byteList = new ArrayList<>();
            ooFile.toAssemble(byteList);
            for(byte by: byteList) {
                System.out.printf("0x%x ", (int)by);
                output.writeByte((int)by);
            }
        } catch (Exception e) {
            // 遇到错误不输出，直接退出
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    
//        if (result.getBoolean("tokenize")) {
//            // tokenize
//            var tokens = new ArrayList<Token>();
//            try {
//                while (true) {
//                    var token = tokenizer.nextToken();
//                    if (token.getTokenType().equals(TokenType.EOF)) {
//                        break;
//                    }
//                    tokens.add(token);
//                }
//            } catch (Exception e) {
//                // 遇到错误不输出，直接退出
//                System.err.println(e);
//                System.exit(0);
//                return;
//            }
//            for (Token token : tokens) {
//                output.println(token.toString());
//            }
//        } else if (result.getBoolean("analyse")) {
//            // analyze
//            var analyzer = new Analyser(tokenizer);
//            List<Instruction> instructions;
//            try {
//                instructions = analyzer.analyse();
//            } catch (Exception e) {
//                // 遇到错误不输出，直接退出
//                System.err.println(e);
//                System.exit(0);
//                return;
//            }
//            for (Instruction instruction : instructions) {
//                output.println(instruction.toString());
//            }
//        } else {
//            System.err.println("Please specify either '--analyse' or '--tokenize'.");
//            System.exit(3);
//        }
//    }

    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("miniplc0-java");
        var parser = builder.build();
        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
