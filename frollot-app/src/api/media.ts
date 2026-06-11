import api from './client';
import { Platform } from 'react-native';

export const mediaApi = {
  uploadImage: async (uri: string, fileName: string): Promise<string> => {
    const formData = new FormData();

    if (Platform.OS === 'web') {
      const response = await fetch(uri);
      const blob = await response.blob();
      formData.append('file', blob, fileName);
    } else {
      const ext = fileName.split('.').pop()?.toLowerCase();
      const mimeType =
        ext === 'png' ? 'image/png' :
        ext === 'webp' ? 'image/webp' :
        ext === 'gif' ? 'image/gif' :
        ext === 'heic' ? 'image/heic' :
        'image/jpeg';
      formData.append('file', {
        uri,
        name: fileName,
        type: mimeType,
      } as any);
    }

    const { data } = await api.post<{ url: string; path: string; uploadedBy: string; filename: string }>('/api/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    // Return the path (e.g. /uploads/xxx.jpg) rather than the absolute URL,
    // so resolveMediaUrl() at display time resolves it for the current platform.
    return data.path || data.url;
  },

  ping: () =>
    api.get<boolean>('/api/ping').then((r) => r.data),
};
