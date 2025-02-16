package org.example;

public class Colortest{
    public static void main(String[] args) {
        ITextDecorator redDecorator = new ColorDecorator("\u001B[31m");  // Red color
        String decoratedText = redDecorator.decorate("Hello, World!");
        System.out.println(decoratedText);
    }
public static class ColorDecorator implements ITextDecorator {
    private String color;

    // Constructor accepts the color code
    public ColorDecorator(String color) {
        this.color = color;
    }

    // Decorates the text by applying the color code
    @Override
    public String decorate(String text) {
        return color + text + "\u001B[0m";  // Apply color and reset it after text
    }
}

}
