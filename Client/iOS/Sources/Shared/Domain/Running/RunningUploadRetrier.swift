import Foundation

actor RunningUploadRetrier {
    private let store: RunningHistoryStore
    private let runService: RunServiceProtocol
    private var isUploading = false

    init(store: RunningHistoryStore = RunningHistoryStore(), runService: RunServiceProtocol) {
        self.store = store
        self.runService = runService
    }

    @discardableResult
    func uploadPending(accessToken: String, currentUserID: String?) async -> [UUID] {
        guard let currentUserID else { return [] }
        guard !isUploading else { return [] }
        isUploading = true
        defer { isUploading = false }

        var uploadedIDs: [UUID] = []
        for pendingUpload in store.pendingUploads()
            where pendingUpload.ownerUserID == currentUserID {
            do {
                try store.markPendingUploadAttempt(recordID: pendingUpload.id)
                _ = try await runService.createRun(record: pendingUpload.record, accessToken: accessToken)
                try store.removePendingUpload(recordID: pendingUpload.id)
                uploadedIDs.append(pendingUpload.id)
            } catch {
                continue
            }
        }
        return uploadedIDs
    }
}
