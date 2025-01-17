/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2021 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text.minimessage.placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A resolver for user-defined placeholders.
 *
 * @since 4.10.0
 */
@FunctionalInterface
public interface PlaceholderResolver {
  /**
   * Constructs a placeholder resolver from a map.
   *
   * <p>The provided map is used as the backing for the returned placeholder resolver.
   * This means that changes to the map will be reflected in the placeholder resolver.</p>
   *
   * @param map the map
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver map(final @NotNull Map<String, Replacement<?>> map) {
    return new MapPlaceholderResolver(Objects.requireNonNull(map, "map"));
  }

  /**
   * Constructs a placeholder resolver from some placeholders.
   *
   * @param placeholders the placeholders
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver placeholders(final @NotNull Placeholder<?> @NotNull ... placeholders) {
    if (Objects.requireNonNull(placeholders, "placeholders").length == 0) return empty();
    return placeholders(Arrays.asList(placeholders));
  }

  /**
   * Constructs a placeholder resolver from some placeholders.
   *
   * @param placeholders the placeholders
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver placeholders(final @NotNull Iterable<? extends Placeholder<?>> placeholders) {
    final Map<String, Placeholder<?>> placeholderMap = new HashMap<>();

    for (final Placeholder<?> placeholder : Objects.requireNonNull(placeholders, "placeholders")) {
      Objects.requireNonNull(placeholder, "placeholders must not contain null elements");
      placeholderMap.put(placeholder.key(), placeholder);
    }

    if (placeholderMap.isEmpty()) return empty();

    return new MapPlaceholderResolver(placeholderMap);
  }

  /**
   * Constructs a placeholder resolver capable of resolving from multiple sources.
   *
   * @param placeholderResolvers the placeholder resolvers
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver combining(final @NotNull PlaceholderResolver @NotNull ... placeholderResolvers) {
    if (Objects.requireNonNull(placeholderResolvers, "placeholderResolvers").length == 1)
      return Objects.requireNonNull(placeholderResolvers[0], "placeholderResolvers must not contain null elements");
    return new GroupedPlaceholderResolver(Arrays.asList(placeholderResolvers));
  }

  /**
   * Constructs a placeholder resolver capable of resolving from multiple sources, in iteration order.
   *
   * <p>The provided iterable is copied. This means changes to the iterable will not reflect in the returned resolver.</p>
   *
   * @param placeholderResolvers the placeholder resolvers
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver combining(final @NotNull Iterable<? extends PlaceholderResolver> placeholderResolvers) {
    final List<PlaceholderResolver> placeholderResolverList = new ArrayList<>();

    for (final PlaceholderResolver placeholderResolver : Objects.requireNonNull(placeholderResolvers, "placeholderResolvers")) {
      placeholderResolverList.add(Objects.requireNonNull(placeholderResolver, "placeholderResolvers cannot contain null elements"));
    }

    final int size = placeholderResolverList.size();
    if (size == 0) return empty();
    if (size == 1) return placeholderResolverList.get(0);
    return new GroupedPlaceholderResolver(placeholderResolvers);
  }

  /**
   * Constructs a placeholder resolver capable of dynamically resolving placeholders.
   *
   * <p>The resolver can return {@code null} to indicate it cannot resolve a placeholder.
   * Once a string to replacement mapping has been created, it will be cached to avoid
   * the cost of recreating the replacement.</p>
   *
   * @param resolver the resolver
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver dynamic(final @NotNull Function<String, Replacement<?>> resolver) {
    return new DynamicPlaceholderResolver(Objects.requireNonNull(resolver, "resolver"));
  }

  /**
   * An empty placeholder resolver that will return {@code null} for all resolve attempts.
   *
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver empty() {
    return EmptyPlaceholderResolver.INSTANCE;
  }

  /**
   * Returns the replacement for a given key, if any exist.
   *
   * <p>This method might be called multiple times during each parse attempt. This is due to the
   * fact that it is used in places to check if a tag is a placeholder or not. Therefore, you
   * should prefer using fixed or cached replacements instead of dynamic construction.</p>
   *
   * @param key the key
   * @return the replacement
   * @since 4.10.0
   */
  @Nullable Replacement<?> resolve(final @NotNull String key);
}
