package dev.spaceseries.spacechat.util.async;

import org.bukkit.Bukkit;

import java.util.function.Function;

/**
 * A class to fetch some data async or fall back to other code when sync
 * @param <S> The source type to fetch data for
 * @param <R> The type to fetch
 */
public class AsyncFetcher<S, R> {

    private final Function<S, R> asyncFetcher;
    private final Function<S, R> primaryThreadFallback;

    public AsyncFetcher(Function<S, R> asyncFetcher, Function<S, R> primaryThreadFallback) {
        this.asyncFetcher = asyncFetcher;
        this.primaryThreadFallback = primaryThreadFallback;
    }

    public R fetch(S source) {
        if (Bukkit.isPrimaryThread()) {
            return primaryThreadFallback.apply(source);
        }
        return asyncFetcher.apply(source);
    }
}
