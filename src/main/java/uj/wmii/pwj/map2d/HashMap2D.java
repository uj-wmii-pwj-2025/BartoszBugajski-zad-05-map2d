package uj.wmii.pwj.map2d;

import java.util.*;
import java.util.function.Function;

public class HashMap2D<R, C, V> implements Map2D<R, C, V>
{
    private final Map<R, Map<C, V>> data = new HashMap<>();
    private int size = 0;

    @Override
    public V put(R rowKey, C columnKey, V value)
    {
        Objects.requireNonNull(rowKey);
        Objects.requireNonNull(columnKey);

        Map<C, V> row = data.computeIfAbsent(rowKey, k -> new HashMap<>());

        if (!row.containsKey(columnKey))
        {
            size++;
        }
        return row.put(columnKey, value);
    }

    @Override
    public V get(R rowKey, C columnKey)
    {
        Map<C, V> row = data.get(rowKey);
        return row == null ? null : row.get(columnKey);
    }

    @Override
    public V getOrDefault(R rowKey, C columnKey, V defaultValue) {
        Map<C, V> row = data.get(rowKey);
        return row == null ? defaultValue : row.getOrDefault(columnKey, defaultValue);
    }

    @Override
    public V remove(R rowKey, C columnKey) {
        Map<C, V> row = data.get(rowKey);
        if (row == null) return null;

        if (row.containsKey(columnKey)) {
            V removed = row.remove(columnKey);
            size--;
            if (row.isEmpty()) {
                data.remove(rowKey);
            }
            return removed;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean nonEmpty() {
        return size > 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        data.clear();
        size = 0;
    }

    @Override
    public Map<C, V> rowView(R rowKey) {
        Map<C, V> row = data.get(rowKey);
        if (row == null) {
            return Collections.emptyMap();
        }
        return Map.copyOf(row);
    }

    @Override
    public Map<R, V> columnView(C columnKey) {
        Map<R, V> result = new HashMap<>();
        data.forEach((r, row) -> {
            if (row.containsKey(columnKey)) {
                result.put(r, row.get(columnKey));
            }
        });
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean containsValue(V value) {
        return data.values().stream().anyMatch(row -> row.containsValue(value));
    }

    @Override
    public boolean containsKey(R rowKey, C columnKey) {
        Map<C, V> row = data.get(rowKey);
        return row != null && row.containsKey(columnKey);
    }

    @Override
    public boolean containsRow(R rowKey) {
        return data.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(C columnKey) {
        return data.values().stream().anyMatch(row -> row.containsKey(columnKey));
    }

    @Override
    public Map<R, Map<C, V>> rowMapView() {
        Map<R, Map<C, V>> result = new HashMap<>();
        data.forEach((r, row) -> result.put(r, Map.copyOf(row)));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<C, Map<R, V>> columnMapView() {
        Map<C, Map<R, V>> result = new HashMap<>();
        for (Map.Entry<R, Map<C, V>> rowEntry : data.entrySet()) {
            R rowKey = rowEntry.getKey();
            for (Map.Entry<C, V> cellEntry : rowEntry.getValue().entrySet()) {
                C colKey = cellEntry.getKey();
                V value = cellEntry.getValue();

                result.computeIfAbsent(colKey, k -> new HashMap<>()).put(rowKey, value);
            }
        }
        result.replaceAll((c, m) -> Collections.unmodifiableMap(m));

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map2D<R, C, V> fillMapFromRow(Map<? super C, ? super V> target, R rowKey) {
        Map<C, V> row = data.get(rowKey);
        if (row != null) target.putAll(row);
        return this;
    }

    @Override
    public Map2D<R, C, V> fillMapFromColumn(Map<? super R, ? super V> target, C columnKey) {
        data.forEach((r, row) -> {
            if (row.containsKey(columnKey)) {
                target.put(r, row.get(columnKey));
            }
        });
        return this;
    }

    @Override
    public Map2D<R, C, V> putAll(Map2D<? extends R, ? extends C, ? extends V> source) {
        source.rowMapView().forEach((r, row) ->
                row.forEach((c, v) -> this.put(r, c, v)));
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToRow(Map<? extends C, ? extends V> source, R rowKey) {
        source.forEach((c, v) -> this.put(rowKey, c, v));
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToColumn(Map<? extends R, ? extends V> source, C columnKey) {
        source.forEach((r, v) -> this.put(r, columnKey, v));
        return this;
    }

    @Override
    public <R2, C2, V2> Map2D<R2, C2, V2> copyWithConversion(
            Function<? super R, ? extends R2> rowFunction,
            Function<? super C, ? extends C2> columnFunction,
            Function<? super V, ? extends V2> valueFunction) {

        Map2D<R2, C2, V2> result = Map2D.createInstance();

        data.forEach((r, row) -> {
            R2 nr = rowFunction.apply(r);
            row.forEach((c, v) -> {
                C2 nc = columnFunction.apply(c);
                V2 nv = valueFunction.apply(v);
                result.put(nr, nc, nv);
            });
        });

        return result;
    }
}