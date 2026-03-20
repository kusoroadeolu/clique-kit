# Clique Syntax Parser

A Java syntax highlighting library that parses and styles Java source code with ANSI color codes. Built on top of [JavaParser](https://github.com/javaparser/javaparser) and the `clique` styling library.

---

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.kusoroadeolu</groupId>
    <artifactId>parser</artifactId>
    <version>0.0.1</version>
</dependency>
```

---

## Usage

### Basic

```java
JavaSyntaxParser parser = new JavaSyntaxParser();
String styled = parser.parse(sourceCode);
System.out.println(styled);
```

### With a theme

```java
AnsiStringParser parser = new JavaSyntaxParser(SyntaxThemes.CATPPUCCIN_MOCHA);
parser.print(sourceCode);
```

### Without line numbers

```java
JavaSyntaxParser parser = new JavaSyntaxParser(false);
String styled = parser.parse(sourceCode);
```

### With both options

```java
JavaSyntaxParser parser = new JavaSyntaxParser(SyntaxThemes.NORD, false);
String styled = parser.parse(sourceCode);
```

---

## Themes

The following built-in themes are available via `SyntaxThemes`:

| Constant                      | Description         |
|-------------------------------|---------------------|
| `SyntaxThemes.DEFAULT`        | IntelliJ-inspired   |
| `SyntaxThemes.CATPPUCCIN_MOCHA` | Catppuccin Mocha  |
| `SyntaxThemes.GRUVBOX`        | Gruvbox Dark        |
| `SyntaxThemes.NORD`           | Nord                |
| `SyntaxThemes.TOKYO_NIGHT`    | Tokyo Night         |

### Custom themes

Implement the `SyntaxTheme` interface to define your own:

```java
public class MyTheme implements SyntaxTheme {
    @Override public AnsiCode keyword()       { return Clique.rgb(255, 100, 100); }
    @Override public AnsiCode string()        { return Clique.rgb(100, 200, 100); }
    @Override public AnsiCode numberLiteral() { return Clique.rgb(100, 150, 255); }
    @Override public AnsiCode comment()       { return Clique.rgb(120, 120, 120); }
    @Override public AnsiCode annotation()    { return Clique.rgb(200, 200, 50);  }
    @Override public AnsiCode method()        { return Clique.rgb(255, 200, 100); }
    @Override public AnsiCode gutter()        { return Clique.rgb(80, 80, 80);    }
    @Override public AnsiCode types()         { return Clique.rgb(170, 180, 200); }
    @Override public AnsiCode constants()     { return Clique.rgb(150, 120, 170); }
}
```

Then pass it to the parser:

```java
JavaSyntaxParser parser = new JavaSyntaxParser(new MyTheme());
```

---

## Token categories

The following token categories are styled:

| Category       | Examples                                  |
|----------------|-------------------------------------------|
| Keywords       | `public`, `void`, `class`, `return`, ...  |
| Strings        | `"hello"`, text blocks, Javadoc           |
| Number literals| `42`, `3.14f`, `0xFF`, ...                |
| Comments       | `//`, `/* */`                             |
| Annotations    | `@Override`, `@SuppressWarnings`, ...     |
| Method names   | Declared method and constructor names     |
| Type references| `String`, `List`, `MyClass`, ...          |
| Constants      | `static final` fields                     |

---

## Incomplete or partial syntax

The parser does a **best-effort** pass on incomplete or malformed code. It will still tokenize and style what it can, but some categories may not render fully:

- **Keywords, strings, literals, comments, and annotations** — these are derived purely from token kinds and will render correctly regardless of whether the code is valid.
- **Method names, type references, and constants** — these require a successful AST walk to identify. On partial or invalid input, JavaParser may not resolve them, so they will fall back to unstyled text.

For example:

```java
// This will highlight correctly except from the method literal, keywords and literals are token-kind based
void main() {
    int a = 1;
```

```java
// 'String' may not get its type color here since AST resolution is incomplete
String result = someService.fetch(userId
```

In short: the more structurally complete your snippet is, the more accurate the highlighting will be. For most real-world use cases, full files, complete method bodies, or class declarations the output should be fully styled.

---

## Requirements

- Java 25+
- JavaParser 3.26.3