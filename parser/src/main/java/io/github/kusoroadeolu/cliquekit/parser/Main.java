package io.github.kusoroadeolu.cliquekit.parser;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.core.display.Component;
import io.github.kusoroadeolu.clique.parser.AnsiStringParser;

public class Main {
    void main(){


        AnsiStringParser parser = new JavaSyntaxParser();

        Component highlighted = () -> parser.parse(codeSnippet);
        Clique.frame()
                .title("[bold]Java[/]")
                .nest(highlighted)
                .render();
    }


    String codeSnippet = """
            package io.github.kusoroadeolu.cliquekit.timer;
            
            import io.github.kusoroadeolu.clique.Clique;
            import io.github.kusoroadeolu.clique.frame.Frame;
            
            public class LiveRenderer {
                private final Frame frame;
                private final Clock clock;
                private final String title;
            
                public LiveRenderer(Configuration config){
                    var time = config.time();
                    title = config.title();
                    if (time == null){
                        Clique.parser().print("[ctp_red]Failed to read [bold] time [/] values from config file");
                        time = Time.DEFAULT;
                    }
                    this.frame = Clique
                            .frame();
            
                    clock = new Clock(time);
                }
            
            
                public void render() throws InterruptedException {
                    frame.title("[ctp_mauve] %s [/]".formatted(title))
                            .nest(clock);
            
                    int lastSize = 0;
                    while (!clock.isDone()) {
                        if (lastSize > 0) System.out.print("\\033[" + lastSize + "A");
            
                        var rendered = frame.get();
                        var lines = rendered.lines().toList();
                        for (var line : lines) System.out.println("\\r" + line);
                        lastSize = lines.size();
                        clock.tick();
                    }
                }
            }
            
            """;
}
