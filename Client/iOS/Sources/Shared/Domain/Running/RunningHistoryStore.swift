import Foundation

final class RunningHistoryStore {
    private struct StoredRunningData: Codable {
        var records: [RunningRecord] = []
        var pendingUploads: [PendingRunUpload] = []
    }

    private static let queue = DispatchQueue(label: "runpamine.running-history-store")
    private let defaults: UserDefaults
    private let fileManager: FileManager
    private let legacyRecordsKey = "runpamine.running-records.v1"
    private let fileURL: URL

    init(defaults: UserDefaults = .standard, fileManager: FileManager = .default) {
        self.defaults = defaults
        self.fileManager = fileManager
        fileURL = Self.defaultFileURL(fileManager: fileManager)
        migrateLegacyRecordsIfNeeded()
    }

    func load() -> [RunningRecord] {
        (try? Self.queue.sync {
            try readStoredData().records
        }) ?? []
    }

    func save(_ records: [RunningRecord]) throws {
        try Self.queue.sync {
            var storedData = try readStoredData()
            storedData.records = records.deduplicatedByID()
            try writeStoredData(storedData)
        }
    }

    func prepend(_ record: RunningRecord) throws {
        try Self.queue.sync {
            var storedData = try readStoredData()
            storedData.records.removeAll { $0.id == record.id }
            storedData.records.insert(record, at: 0)
            try writeStoredData(storedData)
        }
    }

    func pendingUploads() -> [PendingRunUpload] {
        (try? Self.queue.sync {
            try readStoredData().pendingUploads.sortedByCreatedDate()
        }) ?? []
    }

    func pendingUpload(for recordID: UUID) -> PendingRunUpload? {
        try? Self.queue.sync {
            try readStoredData().pendingUploads.first { $0.id == recordID }
        }
    }

    func enqueuePendingUpload(_ record: RunningRecord, ownerUserID: String?, createdAt: Date = Date()) throws {
        guard let ownerUserID else {
            throw RunningHistoryStoreError.missingOwner
        }

        try Self.queue.sync {
            var storedData = try readStoredData()
            storedData.records.removeAll { $0.id == record.id }
            storedData.records.insert(record, at: 0)

            if !storedData.pendingUploads.contains(where: { $0.id == record.id }) {
                storedData.pendingUploads.append(PendingRunUpload(record: record, ownerUserID: ownerUserID, createdAt: createdAt))
            }
            try writeStoredData(storedData)
        }
    }

    func markPendingUploadAttempt(recordID: UUID, attemptedAt: Date = Date()) throws {
        try Self.queue.sync {
            var storedData = try readStoredData()
            guard let index = storedData.pendingUploads.firstIndex(where: { $0.id == recordID }) else { return }
            storedData.pendingUploads[index].lastAttemptAt = attemptedAt
            storedData.pendingUploads[index].attemptCount += 1
            try writeStoredData(storedData)
        }
    }

    func removePendingUpload(recordID: UUID) throws {
        try Self.queue.sync {
            var storedData = try readStoredData()
            storedData.pendingUploads.removeAll { $0.id == recordID }
            try writeStoredData(storedData)
        }
    }

    func removeAll() throws {
        try Self.queue.sync {
            if fileManager.fileExists(atPath: fileURL.path) {
                try fileManager.removeItem(at: fileURL)
            }
            defaults.removeObject(forKey: legacyRecordsKey)
        }
    }

    private func migrateLegacyRecordsIfNeeded() {
        Self.queue.sync {
            guard let data = defaults.data(forKey: legacyRecordsKey),
                  let records = try? JSONDecoder().decode([RunningRecord].self, from: data)
            else {
                return
            }

            let storedData: StoredRunningData
            do {
                storedData = try readStoredData()
            } catch {
                return
            }
            var migratedData = storedData
            migratedData.records = (storedData.records + records).deduplicatedByID()
            do {
                try writeStoredData(migratedData)
                defaults.removeObject(forKey: legacyRecordsKey)
            } catch {
                return
            }
        }
    }

    private func readStoredData() throws -> StoredRunningData {
        guard fileManager.fileExists(atPath: fileURL.path) else { return StoredRunningData() }
        let data = try Data(contentsOf: fileURL)
        return try JSONDecoder().decode(StoredRunningData.self, from: data)
    }

    private func writeStoredData(_ storedData: StoredRunningData) throws {
        try fileManager.createDirectory(
            at: fileURL.deletingLastPathComponent(),
            withIntermediateDirectories: true
        )
        try excludeFromBackup(fileURL.deletingLastPathComponent())

        let data = try JSONEncoder().encode(storedData)
        try data.write(to: fileURL, options: protectedWriteOptions)
        try excludeFromBackup(fileURL)
        try protectFile(at: fileURL)
    }

    private var protectedWriteOptions: Data.WritingOptions {
        #if os(iOS)
        return [.atomic, .completeFileProtectionUntilFirstUserAuthentication]
        #else
        return [.atomic]
        #endif
    }

    private func protectFile(at url: URL) throws {
        #if os(iOS)
        try fileManager.setAttributes(
            [.protectionKey: FileProtectionType.completeUntilFirstUserAuthentication],
            ofItemAtPath: url.path
        )
        #endif
    }

    private func excludeFromBackup(_ url: URL) throws {
        var values = URLResourceValues()
        values.isExcludedFromBackup = true
        var excludedURL = url
        try excludedURL.setResourceValues(values)
    }

    private static func defaultFileURL(fileManager: FileManager) -> URL {
        let baseURL = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first
            ?? fileManager.temporaryDirectory
        return baseURL
            .appendingPathComponent("Runpamine", isDirectory: true)
            .appendingPathComponent("running-history-v2.json", isDirectory: false)
    }
}

enum RunningHistoryStoreError: LocalizedError {
    case missingOwner

    var errorDescription: String? {
        switch self {
        case .missingOwner:
            return "로그인 정보를 확인할 수 없어 러닝 기록을 저장하지 못했습니다."
        }
    }
}

struct PendingRunUpload: Codable, Equatable, Identifiable {
    var id: UUID {
        record.id
    }

    let record: RunningRecord
    let ownerUserID: String?
    let createdAt: Date
    var lastAttemptAt: Date?
    var attemptCount: Int

    init(
        record: RunningRecord,
        ownerUserID: String?,
        createdAt: Date,
        lastAttemptAt: Date? = nil,
        attemptCount: Int = 0
    ) {
        self.record = record
        self.ownerUserID = ownerUserID
        self.createdAt = createdAt
        self.lastAttemptAt = lastAttemptAt
        self.attemptCount = attemptCount
    }
}

private extension Array where Element == RunningRecord {
    func deduplicatedByID() -> [RunningRecord] {
        var seenIDs = Set<UUID>()
        return filter { record in
            seenIDs.insert(record.id).inserted
        }
    }
}

private extension Array where Element == PendingRunUpload {
    func sortedByCreatedDate() -> [PendingRunUpload] {
        sorted {
            if $0.createdAt == $1.createdAt {
                return $0.record.startedAt < $1.record.startedAt
            }
            return $0.createdAt < $1.createdAt
        }
    }
}
