#!/bin/sh
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/api/proto/blockchain.proto -s --output android-sdk/src/main/proto/blockchain.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/api/proto/printable.proto -s --output android-sdk/src/main/proto/printable.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/api/proto/external.proto -s --output android-sdk/src/main/proto/external.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/consensus/api/proto/consensus_client.proto -s --output android-sdk/src/main/proto/consensus_client.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/consensus/api/proto/consensus_common.proto -s --output android-sdk/src/main/proto/consensus_common.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/consensus/api/proto/consensus_peer.proto -s --output android-sdk/src/main/proto/consensus_peer.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/consensus/api/proto/consensus_config.proto -s --output android-sdk/src/main/proto/consensus_config.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/attest/api/proto/attest.proto -s --output android-sdk/src/main/proto/attest.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/report/api/proto/report.proto -s --output android-sdk/src/main/proto/report.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/api/proto/view.proto -s --output android-sdk/src/main/proto/view.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/api/proto/fog_common.proto -s --output android-sdk/src/main/proto/fog_common.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/api/proto/kex_rng.proto -s --output android-sdk/src/main/proto/kex_rng.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/api/proto/ledger.proto -s --output android-sdk/src/main/proto/ledger.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/api/proto/ingest.proto -s --output android-sdk/src/main/proto/ingest.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/fog/api/proto/ingest_common.proto -s --output android-sdk/src/main/proto/ingest_common.proto
curl https://raw.githubusercontent.com/mobilecoinfoundation/mobilecoin/master/api/proto/quorum_set.proto -s --output android-sdk/src/main/proto/quorum_set.proto

