package org.gooseprjkt.bobomb.services

import org.gooseprjkt.bobomb.services.core.Service

typealias Services = List<Service>

fun collectAll(
    repositories: List<() -> List<Service>>,
    listener: (Int, Int, Services) -> Unit
): Services {
    val services = mutableListOf<Service>()

    repositories.run {
        listener(0, size, services)
        forEachIndexed { index, servicesRepository ->
            services.addAll(servicesRepository())
            listener(index + 1, size, services)
        }
    }

    return services
}

fun Services.filter(countryCode: String): Services {
    val countryCodeNum = if (countryCode.isEmpty()) 0 else countryCode.toInt()

    return buildList {
        for (service in this@filter) {
            if (service.countryCodes.isEmpty()) {
                add(service)
                continue
            }

            if (service.countryCodes.contains(countryCodeNum))
                add(service)
        }
    }
}