package org.gfs.chat.stories.settings.my

import org.gfs.chat.database.model.DistributionListPrivacyMode

data class MyStoryPrivacyState(val privacyMode: DistributionListPrivacyMode? = null, val connectionCount: Int = 0)
