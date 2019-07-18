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

import com.palantir.tritium.metrics.registry.MetricName;
import com.palantir.tritium.metrics.registry.TaggedMetricRegistry;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

final class OperatingSystemMetrics {

    static void register(TaggedMetricRegistry registry) {
        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
        registry.gauge(MetricName.builder().safeName("os.load.norm.1").build(),
                () -> osMxBean.getSystemLoadAverage() / osMxBean.getAvailableProcessors());
        registry.gauge(MetricName.builder().safeName("os.load.1").build(), osMxBean::getSystemLoadAverage);
    }

    private OperatingSystemMetrics() {
        throw new UnsupportedOperationException();
    }
}