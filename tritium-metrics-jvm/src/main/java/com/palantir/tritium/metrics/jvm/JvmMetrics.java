/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.tritium.metrics.jvm;

import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.google.common.collect.Maps;
import com.palantir.logsafe.Preconditions;
import com.palantir.tritium.metrics.MetricRegistries;
import com.palantir.tritium.metrics.registry.TaggedMetricRegistry;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/** {@link JvmMetrics} provides a standard set of metrics for debugging java services. */
public final class JvmMetrics {

    /**
     * Registers a default set of metrics.
     *
     * <p>This includes {@link MetricRegistries#registerGarbageCollection(TaggedMetricRegistry)} and
     * {@link MetricRegistries#registerMemoryPools(TaggedMetricRegistry)}.
     *
     * @param registry metric registry
     */
    public static void register(TaggedMetricRegistry registry) {
        Preconditions.checkNotNull(registry, "TaggedMetricRegistry is required");
        MetricRegistries.registerGarbageCollection(registry);
        MetricRegistries.registerMemoryPools(registry);
        InternalJvmMetrics metrics = InternalJvmMetrics.of(registry);
        Jdk9CompatibleFileDescriptorRatioGauge.register(metrics);
        OperatingSystemMetrics.register(registry);
        SafepointMetrics.register(registry);
        registerAttributes(metrics);
        MetricRegistries.registerAll(
                registry, "jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        registerClassLoading(metrics);
        MetricRegistries.registerAll(
                registry,
                "jvm.memory",
                () -> Maps.filterKeys(
                        // Memory pool metrics are already provided by MetricRegistries.registerMemoryPools
                        new MemoryUsageGaugeSet().getMetrics(), name -> !name.startsWith("pools")));
        MetricRegistries.registerAll(registry, "jvm.threads", new ThreadStatesGaugeSet());
    }

    private static void registerAttributes(InternalJvmMetrics metrics) {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        metrics.attributeUptime()
                .javaSpecificationVersion(System.getProperty("java.specification.version", "unknown"))
                .javaVersion(System.getProperty("java.version", "unknown"))
                .javaVersionDate(System.getProperty("java.version.date", "unknown"))
                .javaRuntimeVersion(System.getProperty("java.runtime.version", "unknown"))
                .javaVendorVersion(System.getProperty("java.vendor.version", "unknown"))
                .javaVmVendor(System.getProperty("java.vm.vendor", "unknown"))
                .build(runtimeMxBean::getUptime);
    }

    private static void registerClassLoading(InternalJvmMetrics metrics) {
        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        metrics.classloaderLoaded(classLoadingBean::getTotalLoadedClassCount);
        metrics.classloaderUnloaded(classLoadingBean::getUnloadedClassCount);
    }

    private JvmMetrics() {
        throw new UnsupportedOperationException();
    }
}
